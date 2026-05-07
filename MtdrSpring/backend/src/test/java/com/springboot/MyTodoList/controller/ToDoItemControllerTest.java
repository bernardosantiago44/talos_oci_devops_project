package com.springboot.MyTodoList.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ToDoItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ToDoItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ToDoItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ToDoItemService toDoItemService;

    @Test
    void getAllToDoItemsReturnsItems() throws Exception {
        when(toDoItemService.findAll()).thenReturn(List.of(toDoItem()));

        mockMvc.perform(get("/api/todolist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Write controller tests"))
                .andExpect(jsonPath("$[0].done").value(false));
    }

    @Test
    void getToDoItemByIdReturnsItem() throws Exception {
        when(toDoItemService.getItemById(1)).thenReturn(ResponseEntity.ok(toDoItem()));

        mockMvc.perform(get("/api/todolist/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Write controller tests"))
                .andExpect(jsonPath("$.done").value(false));
    }

    @Test
    void getToDoItemByIdReturnsNotFoundWhenServiceThrows() throws Exception {
        when(toDoItemService.getItemById(404)).thenThrow(new RuntimeException("missing"));

        mockMvc.perform(get("/api/todolist/{id}", 404))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToDoItemReturnsLocationHeader() throws Exception {
        ToDoItem saved = toDoItem();
        saved.setID(7);

        when(toDoItemService.addToDoItem(any(ToDoItem.class))).thenReturn(saved);

        mockMvc.perform(post("/api/todolist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDoItem())))
                .andExpect(status().isOk())
                .andExpect(header().string("location", "7"))
                .andExpect(header().string("Access-Control-Expose-Headers", "location"));
    }

    @Test
    void updateToDoItemReturnsUpdatedItem() throws Exception {
        ToDoItem updated = toDoItem();
        updated.setDone(true);

        when(toDoItemService.updateToDoItem(eq(1), any(ToDoItem.class))).thenReturn(updated);

        mockMvc.perform(put("/api/todolist/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Write controller tests"))
                .andExpect(jsonPath("$.done").value(true));
    }

    @Test
    void updateToDoItemReturnsNotFoundWhenServiceThrows() throws Exception {
        when(toDoItemService.updateToDoItem(any(Integer.class), any(ToDoItem.class)))
                .thenThrow(new RuntimeException("missing"));

        mockMvc.perform(put("/api/todolist/{id}", 404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toDoItem())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteToDoItemReturnsTrue() throws Exception {
        when(toDoItemService.deleteToDoItem(1)).thenReturn(true);

        mockMvc.perform(delete("/api/todolist/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(toDoItemService).deleteToDoItem(1);
    }

    @Test
    void deleteToDoItemReturnsNotFoundWhenServiceThrows() throws Exception {
        doThrow(new RuntimeException("missing")).when(toDoItemService).deleteToDoItem(404);

        mockMvc.perform(delete("/api/todolist/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(false));
    }

    private ToDoItem toDoItem() {
        return new ToDoItem(
                1,
                "Write controller tests",
                OffsetDateTime.parse("2026-04-29T10:00:00-06:00"),
                false
        );
    }
}
