package com.indivaragroup.jatistore.controller.utility;

import com.indivaragroup.jatistore.dto.response.utility.ApiResponse;
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
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        String url = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", Map.of("url", url)));
    }
}
