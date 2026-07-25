package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.user.UpdateUserProfileRequest;
import com.indivaragroup.jatistore.dto.response.module.user.UserProfileResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final AuthRepository authRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) throws CoreThrowHandler {
        log.info("Fetching user profile for email: {}", email);
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", email);
                    return new CoreThrowHandler(RestApiError.GEN_0005);
                });

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Audit(action = "USER_UPDATE_PROFILE", affectedModule = "USER", description = "User updated their profile information")
    @Transactional
    public void updateUserProfile(String email, UpdateUserProfileRequest request) throws CoreThrowHandler {
        log.info("Updating user profile for email: {}", email);
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email {} not found during profile update", email);
                    return new CoreThrowHandler(RestApiError.GEN_0005);
                });

        if (!user.getEmail().equals(request.getEmail()) && authRepository.existsByEmail(request.getEmail())) {
            log.warn("Email {} is already taken", request.getEmail());
            throw new CoreThrowHandler(RestApiError.GEN_0007);
        }

        if (!user.getUsername().equals(request.getUsername()) && authRepository.existsByUsername(request.getUsername())) {
            log.warn("Username {} is already taken", request.getUsername());
            throw new CoreThrowHandler(RestApiError.GEN_0007); 
        }

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        authRepository.save(user);

        log.info("User profile updated successfully for email: {}", email);
    }
}
