package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.tag.CreateTagRequest;
import com.springboot.MyTodoList.dto.tag.TagResponse;
import com.springboot.MyTodoList.dto.tag.UpdateTagRequest;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.TagNotFoundException;
import com.springboot.MyTodoList.model.Tag;
import com.springboot.MyTodoList.repository.TagsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagsServiceTest {
    private static final String TAG_ID = "11111111-1111-1111-1111-111111111111";

    @Mock
    private TagsRepository tagsRepository;

    @InjectMocks
    private TagsService service;

    @Test
    void findAllReturnsTagResponses() {
        when(tagsRepository.findAll()).thenReturn(List.of(tag()));

        List<TagResponse> tags = service.findAll();

        assertThat(tags).hasSize(1);
        assertThat(tags.getFirst().getTagId()).isEqualTo(UUID.fromString(TAG_ID));
        assertThat(tags.getFirst().getName()).isEqualTo("Important");
        assertThat(tags.getFirst().getColor()).isEqualTo("#5BFFAE");
        verify(tagsRepository).findAll();
    }

    @Test
    void findAllReturnsEmptyList() {
        when(tagsRepository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void createTagSavesValidTag() {
        CreateTagRequest request = validCreateRequest();

        when(tagsRepository.existsByName("Important")).thenReturn(false);
        when(tagsRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TagResponse response = service.createTag(request);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagsRepository).save(captor.capture());
        Tag saved = captor.getValue();

        assertThat(saved.getTagId()).isNotBlank();
        assertThat(saved.getName()).isEqualTo("Important");
        assertThat(saved.getColor()).isEqualTo("#5BFFAE");
        assertThat(saved.getDescription()).isEqualTo("High priority work");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(response.getTagId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Important");
    }

    @Test
    void createTagAcceptsNullOptionalFields() {
        CreateTagRequest request = validCreateRequest();
        request.setColor(null);
        request.setDescription(null);

        when(tagsRepository.existsByName("Important")).thenReturn(false);
        when(tagsRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TagResponse response = service.createTag(request);

        assertThat(response.getColor()).isNull();
        assertThat(response.getDescription()).isNull();
    }

    @Test
    void createTagAcceptsHexColorBoundaries() {
        CreateTagRequest request = validCreateRequest();
        request.setColor("#ABC");

        when(tagsRepository.existsByName("Important")).thenReturn(false);
        when(tagsRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.createTag(request).getColor()).isEqualTo("#ABC");

        request.setColor("#AABBCCDD");
        assertThat(service.createTag(request).getColor()).isEqualTo("#AABBCCDD");
    }

    @Test
    void createTagChecksDuplicateNameAfterTrimming() {
        CreateTagRequest request = validCreateRequest();
        request.setName(" Important ");

        when(tagsRepository.existsByName("Important")).thenReturn(true);

        assertThatThrownBy(() -> service.createTag(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Tag with that name already exists.");

        verify(tagsRepository, never()).save(any());
    }

    @Test
    void createTagRejectsInvalidHexColor() {
        CreateTagRequest request = validCreateRequest();
        request.setColor("#12");

        when(tagsRepository.existsByName("Important")).thenReturn(false);

        assertThatThrownBy(() -> service.createTag(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid color: #12");

        verify(tagsRepository, never()).save(any());
    }

    @Test
    void updateTagAppliesProvidedFields() {
        Tag existing = tag();
        UpdateTagRequest request = validUpdateRequest();

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);
        when(tagsRepository.findById(TAG_ID)).thenReturn(Optional.of(existing));
        when(tagsRepository.existsByName("Backlog")).thenReturn(false);
        when(tagsRepository.save(existing)).thenReturn(existing);

        TagResponse response = service.updateTag(TAG_ID, request);

        assertThat(existing.getName()).isEqualTo("Backlog");
        assertThat(existing.getColor()).isEqualTo("#ABC");
        assertThat(existing.getDescription()).isEqualTo("Needs grooming");
        assertThat(response.getName()).isEqualTo("Backlog");
        verify(tagsRepository).save(existing);
    }

    @Test
    void updateTagLeavesNullFieldsUnchanged() {
        Tag existing = tag();
        UpdateTagRequest request = new UpdateTagRequest();

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);
        when(tagsRepository.findById(TAG_ID)).thenReturn(Optional.of(existing));
        when(tagsRepository.save(existing)).thenReturn(existing);

        TagResponse response = service.updateTag(TAG_ID, request);

        assertThat(response.getName()).isEqualTo("Important");
        assertThat(response.getColor()).isEqualTo("#5BFFAE");
        assertThat(response.getDescription()).isEqualTo("High priority work");
        verify(tagsRepository, never()).existsByName(any());
    }

    @Test
    void updateTagThrowsWhenTagDoesNotExist() {
        UpdateTagRequest request = validUpdateRequest();

        when(tagsRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.updateTag("missing", request))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tag with id missing not found.");

        verify(tagsRepository, never()).findById(any());
        verify(tagsRepository, never()).save(any());
    }

    @Test
    void updateTagThrowsWhenFindByIdReturnsEmptyAfterExistsCheck() {
        UpdateTagRequest request = validUpdateRequest();

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);
        when(tagsRepository.findById(TAG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTag(TAG_ID, request))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tag with id " + TAG_ID + " not found.");

        verify(tagsRepository, never()).save(any());
    }

    @Test
    void updateTagRejectsBlankName() {
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName(" ");

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateTag(TAG_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Name can not be empty.");

        verify(tagsRepository, never()).findById(any());
        verify(tagsRepository, never()).save(any());
    }

    @Test
    void updateTagRejectsBlankColor() {
        UpdateTagRequest request = new UpdateTagRequest();
        request.setColor(" ");

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateTag(TAG_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Color must not be blank.");

        verify(tagsRepository, never()).findById(any());
        verify(tagsRepository, never()).save(any());
    }

    @Test
    void updateTagRejectsInvalidHexColor() {
        UpdateTagRequest request = new UpdateTagRequest();
        request.setColor("blue");

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateTag(TAG_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid color: blue");

        verify(tagsRepository, never()).findById(any());
        verify(tagsRepository, never()).save(any());
    }

    @Test
    void updateTagRejectsDuplicateName() {
        UpdateTagRequest request = validUpdateRequest();

        when(tagsRepository.existsById(TAG_ID)).thenReturn(true);
        when(tagsRepository.findById(TAG_ID)).thenReturn(Optional.of(tag()));
        when(tagsRepository.existsByName("Backlog")).thenReturn(true);

        assertThatThrownBy(() -> service.updateTag(TAG_ID, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Tag with that name already exists.");

        verify(tagsRepository, never()).save(any());
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
        request.setName("Backlog");
        request.setColor("#ABC");
        request.setDescription("Needs grooming");
        return request;
    }

    private Tag tag() {
        Tag tag = new Tag();
        tag.setTagId(TAG_ID);
        tag.setName("Important");
        tag.setColor("#5BFFAE");
        tag.setDescription("High priority work");
        tag.setCreatedAt(OffsetDateTime.parse("2026-04-29T10:00:00-06:00"));
        return tag;
    }
}
