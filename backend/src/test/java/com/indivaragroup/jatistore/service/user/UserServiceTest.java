package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.user.UpdateUserProfileRequest;
import com.indivaragroup.jatistore.dto.response.module.user.UserProfileResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserProfile_ShouldReturnProfile() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setPhoneNumber("123456");

        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile("test@test.com");

        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUserProfile_ShouldThrow_WhenNotFound() {
        when(authRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThrows(CoreThrowHandler.class, () -> userService.getUserProfile("notfound@test.com"));
    }

    @Test
    void updateUserProfile_ShouldUpdate() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFullName("New Name");
        request.setPhoneNumber("654321");

        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(authRepository.existsByUsername("newuser")).thenReturn(false);

        userService.updateUserProfile("test@test.com", request);

        assertEquals("new@test.com", user.getEmail());
        assertEquals("newuser", user.getUsername());
        assertEquals("New Name", user.getFullName());
        verify(authRepository, times(1)).save(user);
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenEmailTaken() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setEmail("new@test.com");

        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authRepository.existsByEmail("new@test.com")).thenReturn(true);

        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> userService.updateUserProfile("test@test.com", request));
        assertEquals(RestApiError.GEN_0007.getCode(), ex.getCode());
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenUsernameTaken() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setEmail("test@test.com"); // same email
        request.setUsername("newuser");

        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authRepository.existsByUsername("newuser")).thenReturn(true);

        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> userService.updateUserProfile("test@test.com", request));
        assertEquals(RestApiError.GEN_0007.getCode(), ex.getCode());
    }
    
    @Test
    void updateUserProfile_ShouldThrow_WhenNotFound() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        when(authRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(CoreThrowHandler.class, () -> userService.updateUserProfile("test@test.com", request));
    }
}
