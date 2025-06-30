package com.example.filesharing.ServiceTests;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import com.example.filesharing.Service.FileControlService;

public class FileControlServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private FileControlService fileControlService;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Установим значение bucketName вручную через reflection
        Field field = FileControlService.class.getDeclaredField("bucketName");
        field.setAccessible(true);
        field.set(fileControlService, bucketName);
    }

    @Test
    void testUploadFile() {
        String key = "file.txt";
        byte[] content = "Hello".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);
        long contentLength = content.length;
        String contentType = "text/plain";

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        fileControlService.uploadFile(key, inputStream, contentLength, contentType);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
        assertEquals(contentLength, capturedRequest.contentLength());
        assertEquals(contentType, capturedRequest.contentType());
    }

    @Test
    void testDownloadFile() {
        String key = "file.txt";

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockResponseStream = mock(ResponseInputStream.class);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseStream);

        InputStream result = fileControlService.downloadFile(key);

        assertNotNull(result);
        assertEquals(mockResponseStream, result);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(requestCaptor.capture());

        GetObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
    }

    @Test
    void testDeleteFile() {
        String key = "file.txt";

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        fileControlService.deleteFile(key);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
    }
}
