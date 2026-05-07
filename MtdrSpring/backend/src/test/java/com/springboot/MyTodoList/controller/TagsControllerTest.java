package com.springboot.MyTodoList.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.dto.tag.CreateTagRequest;
import com.springboot.MyTodoList.dto.tag.TagResponse;
import com.springboot.MyTodoList.dto.tag.UpdateTagRequest;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.TagNotFoundException;
import com.springboot.MyTodoList.repository.TagsRepository;
import com.springboot.MyTodoList.service.TagsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagsController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagsControllerTest {
    private static final UUID TAG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagsService tagsService;

    @Test
    void getReturnsTags() throws Exception {
        when(tagsService.findAll()).thenReturn(List.of(tagResponse()));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagId").value(TAG_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("Important"))
                .andExpect(jsonPath("$[0].color").value("#5BFFAE"))
                .andExpect(jsonPath("$[0].description").value("High priority work"));
    }

    @Test
    void createTagReturnsCreatedTag() throws Exception {
        CreateTagRequest request = validCreateRequest();

        when(tagsService.createTag(any(CreateTagRequest.class))).thenReturn(tagResponse());

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tagId").value(TAG_ID.toString()))
                .andExpect(jsonPath("$.name").value("Important"))
                .andExpect(jsonPath("$.color").value("#5BFFAE"));
    }

    @Test
    void createTagAcceptsMaximumLengthNameBoundary() throws Exception {
        CreateTagRequest request = validCreateRequest();
        request.setName("a".repeat(100));

        when(tagsService.createTag(any(CreateTagRequest.class))).thenReturn(new TagResponse(
                TAG_ID,
                request.getName(),
                "#ABC",
                null,
                createdAt()
        ));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    void createTagRejectsBlankNameBeforeServiceCall() throws Exception {
        CreateTagRequest request = validCreateRequest();
        request.setName(" ");

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.name").exists());

        verify(tagsService, never()).createTag(any());
    }

    @Test
    void createTagRejectsNameLongerThanMaximum() throws Exception {
        CreateTagRequest request = validCreateRequest();
        request.setName("a".repeat(101));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.name").exists());

        verify(tagsService, never()).createTag(any());
    }

    @Test
    void createTagReturnsBusinessRuleError() throws Exception {
        CreateTagRequest request = validCreateRequest();
        request.setName("Important");
        
        when(tagsService.createTag(any(CreateTagRequest.class)))
                .thenThrow(new BusinessRuleException("Tag with that name already exists."));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Tag with that name already exists."));
    }

    @Test
    void updateTagReturnsUpdatedTag() throws Exception {
        UpdateTagRequest request = validUpdateRequest();

        when(tagsService.updateTag(eq(TAG_ID.toString()), any(UpdateTagRequest.class)))
                .thenReturn(tagResponse());

        mockMvc.perform(patch("/api/tags/{id}", TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagId").value(TAG_ID.toString()))
                .andExpect(jsonPath("$.name").value("Important"));
    }

    @Test
    void updateTagReturnsNotFound() throws Exception {
        UpdateTagRequest request = validUpdateRequest();

        when(tagsService.updateTag(eq("missing"), any(UpdateTagRequest.class)))
                .thenThrow(new TagNotFoundException("missing"));

        mockMvc.perform(patch("/api/tags/{id}", "missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Tag with id missing not found."));
    }

    @Test
    void updateTagRejectsDescriptionLongerThanMaximum() throws Exception {
        UpdateTagRequest request = validUpdateRequest();
        request.setDescription("a".repeat(301));

        mockMvc.perform(patch("/api/tags/{id}", TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fields.description").exists());

        verify(tagsService, never()).updateTag(any(), any());
    }

    private CreateTagRequest validCreateRequest() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Important");
        request.setColor("#5BFFAE");
        request.setDescription("High priority work");
        return request;
    }

    private UpdateTagRequest validUpdateRequest() {
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("Important");
        request.setColor("#5BFFAE");
        request.setDescription("High priority work");
        return request;
    }

    private TagResponse tagResponse() {
        return new TagResponse(
                TAG_ID,
                "Important",
                "#5BFFAE",
                "High priority work",
                createdAt()
        );
    }

    private OffsetDateTime createdAt() {
        return OffsetDateTime.parse("2026-04-29T10:00:00-06:00");
    }
}
