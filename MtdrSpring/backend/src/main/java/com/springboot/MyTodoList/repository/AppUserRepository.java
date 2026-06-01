package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByTelegramUserId(String telegramUserId);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTelegramUserId(String telegramUserId);

    boolean existsByTelegramUserIdAndUserIdNot(String telegramUserId, String userId);

    List<AppUser> findAllByOrderByNameAsc();
}
