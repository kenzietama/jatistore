package com.indivaragroup.jatistore.service.utility;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {

    private CloudinaryService cloudinaryService;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to set the private Cloudinary instance since CloudinaryService initializes it in the constructor
        cloudinaryService = new CloudinaryService("cloudinary://fake");
        java.lang.reflect.Field field = CloudinaryService.class.getDeclaredField("cloudinary");
        field.setAccessible(true);
        field.set(cloudinaryService, cloudinary);
    }

    @Test
    void uploadImage_shouldReturnSecureUrl() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(cloudinary.uploader()).thenReturn(uploader);
        
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/fake/image/upload/v1234/test.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // Act
        String result = cloudinaryService.uploadImage(file);

        // Assert
        assertEquals("https://res.cloudinary.com/fake/image/upload/v1234/test.jpg", result);
        verify(uploader, times(1)).upload(any(byte[].class), anyMap());
    }
}
