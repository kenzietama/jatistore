package com.indivaragroup.jatistore.dto.request.module.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is mandatory")
    @Size(max = 255, message = "Maximum length is 255 characters")
    private String name;
}
