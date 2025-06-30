package com.example.filesharing.ServiceTests;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import com.example.filesharing.Entities.FileData;
import com.example.filesharing.Repositories.FileRepository;
import com.example.filesharing.Service.FileCleanUpService;
import com.example.filesharing.Service.FileControlService;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class FileCleanUpServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileControlService fileControlService;

    @InjectMocks
    private FileCleanUpService fileCleanUpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteOldFiles() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FileData file1 = FileData.builder().s3Key("old/file1.txt").createdAt(now.minusDays(35)).build();
        FileData file2 = FileData.builder().s3Key("old/file2.txt").createdAt(now.minusDays(40)).build();
        List<FileData> oldFiles = List.of(file1, file2);

        when(fileRepository.findOldFiles(any(LocalDateTime.class))).thenReturn(oldFiles);

        // When
        fileCleanUpService.deleteOldFiles();

        // Then
        verify(fileControlService).deleteFile("old/file1.txt");
        verify(fileControlService).deleteFile("old/file2.txt");

        verify(fileRepository).delete(file1);
        verify(fileRepository).delete(file2);
    }

    @Test
    void testDeleteOldFiles_whenNoneFound() {
        when(fileRepository.findOldFiles(any(LocalDateTime.class))).thenReturn(List.of());

        fileCleanUpService.deleteOldFiles();

        verify(fileControlService, never()).deleteFile(any());
        verify(fileRepository, never()).delete(any());
    }
}

