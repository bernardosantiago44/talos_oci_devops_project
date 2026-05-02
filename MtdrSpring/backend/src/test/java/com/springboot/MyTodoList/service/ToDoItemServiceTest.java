package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.repository.ToDoItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToDoItemServiceTest {
    @Mock
    private ToDoItemRepository toDoItemRepository;

    @InjectMocks
    private ToDoItemService service;

    @Test
    void findAllReturnsRepositoryItems() {
        ToDoItem item = toDoItem();
        when(toDoItemRepository.findAll()).thenReturn(List.of(item));

        assertThat(service.findAll()).containsExactly(item);
        verify(toDoItemRepository).findAll();
    }

    @Test
    void getItemByIdReturnsOkWhenItemExists() {
        ToDoItem item = toDoItem();
        when(toDoItemRepository.findById(1)).thenReturn(Optional.of(item));

        var response = service.getItemById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(item);
    }

    @Test
    void getItemByIdReturnsNotFoundWhenItemDoesNotExist() {
        when(toDoItemRepository.findById(404)).thenReturn(Optional.empty());

        var response = service.getItemById(404);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getToDoItemByIdReturnsItemWhenItExists() {
        ToDoItem item = toDoItem();
        when(toDoItemRepository.findById(1)).thenReturn(Optional.of(item));

        assertThat(service.getToDoItemById(1)).isSameAs(item);
    }

    @Test
    void getToDoItemByIdReturnsNullWhenItemDoesNotExist() {
        when(toDoItemRepository.findById(404)).thenReturn(Optional.empty());

        assertThat(service.getToDoItemById(404)).isNull();
    }

    @Test
    void addToDoItemSavesItem() {
        ToDoItem item = toDoItem();
        when(toDoItemRepository.save(item)).thenReturn(item);

        assertThat(service.addToDoItem(item)).isSameAs(item);
        verify(toDoItemRepository).save(item);
    }

    @Test
    void deleteToDoItemReturnsTrueWhenDeleteSucceeds() {
        assertThat(service.deleteToDoItem(1)).isTrue();
        verify(toDoItemRepository).deleteById(1);
    }

    @Test
    void deleteToDoItemReturnsFalseWhenRepositoryThrows() {
        doThrow(new RuntimeException("missing")).when(toDoItemRepository).deleteById(404);

        assertThat(service.deleteToDoItem(404)).isFalse();
    }

    @Test
    void updateToDoItemSavesUpdatedFieldsWhenItemExists() {
        ToDoItem existing = new ToDoItem(1, "Old description", createdAt(), false);
        ToDoItem updates = new ToDoItem(99, "New description", createdAt(), true);

        when(toDoItemRepository.findById(1)).thenReturn(Optional.of(existing));
        when(toDoItemRepository.save(existing)).thenReturn(existing);

        ToDoItem result = service.updateToDoItem(1, updates);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getID()).isEqualTo(1);
        assertThat(existing.getDescription()).isEqualTo("New description");
        assertThat(existing.getCreation_ts()).isEqualTo(createdAt());
        assertThat(existing.isDone()).isTrue();
        verify(toDoItemRepository).save(existing);
    }

    @Test
    void updateToDoItemReturnsNullWhenItemDoesNotExist() {
        when(toDoItemRepository.findById(404)).thenReturn(Optional.empty());

        assertThat(service.updateToDoItem(404, toDoItem())).isNull();
        verify(toDoItemRepository, never()).save(any());
    }

    private ToDoItem toDoItem() {
        return new ToDoItem(1, "Write service tests", createdAt(), false);
    }

    private OffsetDateTime createdAt() {
        return OffsetDateTime.parse("2026-04-29T10:00:00-06:00");
    }
}
