package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.AppUserSummary;
import com.springboot.MyTodoList.service.AppUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appusers")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUserSummary> getAll() {
        return appUserService.findAll();
    }
}
