package com.indivaragroup.jatistore.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;
import java.time.ZonedDateTime;

@Entity
@Table(name = "mst_users")
@Data
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}
