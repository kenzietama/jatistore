package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthRepositoryTest {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testExistsByEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhoneNumber("08123456789");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Test User");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = authRepository.existsByEmail("test@example.com");
        assertThat(exists).isTrue();

        boolean notExists = authRepository.existsByEmail("other@example.com");
        assertThat(notExists).isFalse();
    }

    @Test
    void testExistsByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhoneNumber("08123456789");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Test User");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = authRepository.existsByUsername("testuser");
        assertThat(exists).isTrue();

        boolean notExists = authRepository.existsByUsername("otheruser");
        assertThat(notExists).isFalse();
    }

    @Test
    void testExistsByPhoneNumber() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhoneNumber("08123456789");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Test User");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = authRepository.existsByPhoneNumber("08123456789");
        assertThat(exists).isTrue();

        boolean notExists = authRepository.existsByPhoneNumber("08987654321");
        assertThat(notExists).isFalse();
    }
}
