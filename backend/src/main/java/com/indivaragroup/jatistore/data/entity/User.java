package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.table.schema.UserVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = UserVariable.TABLE_MST_USERS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = UserVariable.COLUMN_MST_USERS_ID, nullable = false)
    private UUID id;

    @Column(name = UserVariable.COLUMN_MST_USERS_USERNAME, nullable = false, unique = true)
    private String username;

    @Column(name = UserVariable.COLUMN_MST_USERS_EMAIL, nullable = false, unique = true)
    private String email;

    @Column(name = UserVariable.COLUMN_MST_USERS_PHONE_NUMBER, nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = UserVariable.COLUMN_MST_USERS_PASSWORD_HASH, nullable = false)
    private String passwordHash;

    @Column(name = UserVariable.COLUMN_MST_USERS_FULL_NAME, nullable = false)
    private String fullName;

    @Column(name = UserVariable.COLUMN_MST_USERS_DATE_OF_BIRTH)
    private LocalDate dateOfBirth;

    @Column(name = UserVariable.COLUMN_MST_USERS_CREATED_AT, nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = UserVariable.COLUMN_MST_USERS_UPDATED_AT, nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
