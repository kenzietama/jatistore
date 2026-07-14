package com.indivaragroup.jatistore.controller.utility;

import com.indivaragroup.jatistore.service.utility.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @Test
    void uploadImage_shouldReturnUrl() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        when(cloudinaryService.uploadImage(any())).thenReturn("https://secure.url/test.jpg");

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/utility/upload-image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"))
                .andExpect(jsonPath("$.data.url").value("https://secure.url/test.jpg"));
    }
}
