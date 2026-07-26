package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.request.user.UpdateUserProfileRequest;
import com.indivaragroup.jatistore.dto.response.module.user.UserProfileResponse;
import com.indivaragroup.jatistore.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    
    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;
    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;
    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;
    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    
    @Autowired
    private UserController userController;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.springframework.security.core.userdetails.User mockUserDetails = 
            new org.springframework.security.core.userdetails.User("test@test.com", "pwd", java.util.Collections.emptyList());
        
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(org.springframework.security.core.userdetails.UserDetails.class);
                    }
                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.web.method.support.ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        return mockUserDetails;
                    }
                })
                .build();
    }

    @Test
    void getUserProfile_ShouldReturnProfile() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .email("test@test.com")
                .username("testuser")
                .build();

        when(userService.getUserProfile("test@test.com")).thenReturn(profile);
        
        mockMvc.perform(get("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void updateUserProfile_ShouldReturnSuccess() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFullName("New Name");
        request.setPhoneNumber("1234567890");

        doNothing().when(userService).updateUserProfile(eq("test@test.com"), any(UpdateUserProfileRequest.class));

        mockMvc.perform(put("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile updated successfully."));
    }
}
