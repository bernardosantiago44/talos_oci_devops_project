package com.springboot.MyTodoList.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "APP_USER", schema = "CHATBOT_USER")
public class AppUser {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TELEGRAM_USER_ID")
    private String telegramUserId;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @JsonIgnore
    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;

    public   AppUser() {}

    @PrePersist
    public void prePersist() {
        if (userId == null || userId.isBlank()) {
            userId = UUID.randomUUID().toString();
        }

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

}
