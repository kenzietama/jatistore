package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.table.schema.TokenVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = TokenVariable.TABLE_TRX_TOKEN)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = TokenVariable.COLUMN_TRX_TOKENS_ID, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TokenVariable.COLUMN_TRX_TOKENS_USER_ID, nullable = false, unique = true)
    private User user;

    @Column(name = TokenVariable.COLUMN_TRX_TOKENS_TOKEN, nullable = false)
    private String token;

    @Column(name = TokenVariable.COLUMN_TRX_TOKENS_EXPIRES_AT, nullable = false)
    private Instant expiresAt;

    @Column(name = TokenVariable.COLUMN_TRX_TOKENS_LAST_UPDATED_AT, nullable = false)
    private Instant lastUpdatedAt;

    @PrePersist
    protected void onCreate() {
        lastUpdatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = Instant.now();
    }
}