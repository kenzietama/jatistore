package com.indivaragroup.jatistore.controller.utility;

import com.indivaragroup.jatistore.service.utility.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/utility")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload-image")
    public com.indivaragroup.jatistore.dto.response.RestApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws com.indivaragroup.jatistore.exception.CoreThrowHandler {
        try {
            String url = cloudinaryService.uploadImage(file);
            return com.indivaragroup.jatistore.dto.response.RestApiResponse.success(Map.of("url", url));
        } catch (Exception e) {
            throw new com.indivaragroup.jatistore.exception.CoreThrowHandler(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }
}
