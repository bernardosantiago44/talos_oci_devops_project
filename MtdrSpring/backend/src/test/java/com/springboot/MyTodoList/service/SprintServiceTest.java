package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.exception.SprintNotFoundException;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {
    @Mock
    private SprintRepository sprintRepository;

    @InjectMocks
    private SprintService service;

    @Test
    void findAllReturnsSprintResponses() {
        when(sprintRepository.findAll()).thenReturn(List.of(sprint()));

        List<SprintResponse> sprints = service.findAll();

        assertThat(sprints).hasSize(1);
        assertThat(sprints.getFirst().sprintId()).isEqualTo("sprint-1");
        assertThat(sprints.getFirst().teamId()).isEqualTo("team-1");
        assertThat(sprints.getFirst().name()).isEqualTo("Sprint 1");
        assertThat(sprints.getFirst().status()).isEqualTo("ACTIVE");
        verify(sprintRepository).findAll();
    }

    @Test
    void findByIdReturnsSprintResponse() {
        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint()));

        SprintResponse response = service.findById("sprint-1");

        assertThat(response.sprintId()).isEqualTo("sprint-1");
        assertThat(response.goal()).isEqualTo("Deliver analytics dashboard");
        assertThat(response.createdByUserId()).isEqualTo("creator-1");
    }

    @Test
    void findByIdThrowsWhenSprintDoesNotExist() {
        when(sprintRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("missing"))
                .isInstanceOf(SprintNotFoundException.class)
                .hasMessage("Sprint not found: missing");
    }

    private Sprint sprint() {
        Sprint sprint = new Sprint();
        sprint.setSprintId("sprint-1");
        sprint.setTeamId("team-1");
        sprint.setName("Sprint 1");
        sprint.setGoal("Deliver analytics dashboard");
        sprint.setStartDate(LocalDate.of(2026, 4, 1));
        sprint.setEndDate(LocalDate.of(2026, 4, 15));
        sprint.setStatus("ACTIVE");
        sprint.setCreatedAt(OffsetDateTime.parse("2026-04-01T09:00:00-06:00"));
        sprint.setCreatedByUserId("creator-1");
        return sprint;
    }
}
