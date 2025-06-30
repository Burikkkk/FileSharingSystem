package com.example.filesharing.Controllers;

import com.example.filesharing.Entities.FileData;
import com.example.filesharing.Entities.User;
import com.example.filesharing.Repositories.FileRepository;
import com.example.filesharing.Service.FileControlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileControlService fileControlService;
    private final FileRepository fileRepository;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) throws IOException {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.warn("Попытка загрузки файла без авторизации");
            return "redirect:/login";
        }

        String safeFilename = Paths.get(Objects.requireNonNull(file.getOriginalFilename())).getFileName().toString();
        log.info("Пользователь [{}] начал загрузку файла: {}", user.getUsername(), safeFilename);

        String token = UUID.randomUUID().toString();
        String key = "uploads/" + LocalDate.now() + "/" + UUID.randomUUID();

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            fileControlService.uploadFile(key, inputStream, file.getSize(), contentType);
            log.info("Файл [{}] успешно загружен в хранилище с ключом: {}", safeFilename, key);
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла в хранилище", e);
            redirectAttributes.addFlashAttribute("error", "File upload error");
            return "redirect:/welcome";
        }

        FileData fileData = FileData.builder()
                .filename(safeFilename)
                .s3Key(key)
                .token(token)
                .downloaded(false)
                .owner(user)
                .build();

        try {
            fileRepository.save(fileData);
            log.info("Информация о файле сохранена в БД: fileId={}, token={}", fileData.getId(), token);
        } catch (Exception e) {
            log.error("Ошибка при сохранении файла в БД", e);
            redirectAttributes.addFlashAttribute("error", "Error saving file");
            return "redirect:/welcome";
        }

        redirectAttributes.addFlashAttribute("link", "/download?token=" + token);
        return "redirect:/welcome";
    }

    @GetMapping("/download")
    public String downloadFile(@RequestParam String token,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) throws IOException {

        log.info("Попытка загрузки файла с токеном: {}", token);

        FileData fileData = fileRepository.findByToken(token)
                .orElse(null);

        if (fileData == null) {
            log.warn("Файл с токеном {} не найден", token);
            redirectAttributes.addFlashAttribute("error", "❌ File not found or link is invalid");
            return "redirect:/welcome";
        }

        if (fileData.isDownloaded()) {
            log.warn("Файл с токеном {} уже был скачан ранее", token);
            redirectAttributes.addFlashAttribute("error", "⚠️ This file has already been downloaded");
            return "redirect:/welcome";
        }

        try (InputStream s3InputStream = fileControlService.downloadFile(fileData.getS3Key())) {

            log.info("Файл [{}] найден. Начинается передача клиенту", fileData.getFilename());

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileData.getFilename() + "\"");

            s3InputStream.transferTo(response.getOutputStream());
            response.flushBuffer();

            log.info("Файл [{}] успешно отправлен клиенту", fileData.getFilename());

        } catch (Exception e) {
            log.error("Ошибка при скачивании файла из хранилища", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error downloading file");
            return "redirect:/welcome";
        }

        try {
            fileControlService.deleteFile(fileData.getS3Key());
            log.info("Файл {} удалён из хранилища", fileData.getS3Key());
        } catch (Exception e) {
            log.error("Ошибка при удалении файла из хранилища", e);
        }

        fileData.setDownloaded(true);
        try {
            fileRepository.save(fileData);
            log.info("Флаг скачивания установлен для файла {}", fileData.getId());
        } catch (Exception e) {
            log.error("Ошибка при обновлении флага скачивания в БД", e);
        }

        return null;
    }
}
