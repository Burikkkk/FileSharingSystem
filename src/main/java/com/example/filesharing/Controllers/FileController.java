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
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

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
        if (user == null) return "redirect:/login";

        UUID fileId = UUID.randomUUID();
        String key = "uploads/" + fileId;
        String token = UUID.randomUUID().toString();

        fileControlService.uploadFile(key, file.getInputStream(), file.getSize(), file.getContentType());

        FileData fileData = FileData.builder()
                .id(fileId)
                .filename(file.getOriginalFilename())
                .s3Key(key)
                .token(token)
                .downloaded(false)
                .owner(user)
                .build();

        fileRepository.save(fileData);

        redirectAttributes.addFlashAttribute("link", "/download?token=" + token);
        return "redirect:/welcome";
    }

    @GetMapping("/download")
    public void downloadFile(@RequestParam String token,
                             HttpServletResponse response) throws IOException {

        FileData fileData = fileRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (fileData.isDownloaded()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Файл уже был скачан");
        }

        try (InputStream s3InputStream = fileControlService.downloadFile(fileData.getS3Key())) {

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileData.getFilename() + "\"");

            s3InputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        }

        fileControlService.deleteFile(fileData.getS3Key());

        fileData.setDownloaded(true);
        fileRepository.save(fileData);
    }
}
