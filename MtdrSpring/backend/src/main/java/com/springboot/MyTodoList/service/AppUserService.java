package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.AppUser;
import com.springboot.MyTodoList.model.AppUserSummary;
import com.springboot.MyTodoList.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<AppUserSummary> findAll() {
        return appUserRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private AppUserSummary toSummary(AppUser user) {
        return new AppUserSummary(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getTelegramUserId()
        );
    }
}
