package com.example.filesharing.Service;


import com.example.filesharing.Entities.FileData;
import com.example.filesharing.Repositories.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileCleanUpService {

    private final FileRepository fileRepository;
    private final FileControlService fileControlService;

    @Scheduled(cron = "0 0 3 * * ?") // каждый день в 3:00 утра
    public void deleteOldFiles() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<FileData> oldFiles = fileRepository.findOldFiles(threshold);

        for (FileData file : oldFiles) {
            fileControlService.deleteFile(file.getS3Key());
            fileRepository.delete(file);
        }
    }
}

