package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.AppUserSummary;
import com.springboot.MyTodoList.repository.AppUserRepository;
import com.springboot.MyTodoList.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserService service;

    @Test
    void findAllReturnsOrderedUserSummaries() {
        when(appUserRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(TestDataFactory.appUser("user-1")));

        List<AppUserSummary> users = service.findAll();

        assertThat(users).containsExactly(new AppUserSummary(
                "user-1",
                "Ada Lovelace",
                "user-1@example.com",
                "telegram-user-1"
        ));
    }

    @Test
    void findAllReturnsEmptyList() {
        when(appUserRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }
}
