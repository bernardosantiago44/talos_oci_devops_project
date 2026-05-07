package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

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

    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;

    public   AppUser() {}

}
