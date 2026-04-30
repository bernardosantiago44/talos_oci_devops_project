package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.AppUserSummary;
import com.springboot.MyTodoList.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appusers")
@Tag(name = "App Users", description = "Application users available for assignment and time tracking.")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    @Operation(summary = "List app users", description = "Returns all application users ordered by name.")
    public List<AppUserSummary> getAll() {
        return appUserService.findAll();
    }
}
