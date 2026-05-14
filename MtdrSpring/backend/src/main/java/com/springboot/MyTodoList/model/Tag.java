package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.OffsetDateTime;

@Entity
@Table(name = "TAG")
@Schema(description = "Tags for categorizing work items")
@Getter
@Setter
public final class Tag {
    @Id @Column(name = "tag_id")
    private String tagId;
    
    @Column(name = "name") @Length(max = 100)
    @NonNull
    private String name;
    
    @Column(name = "color") @Nullable
    @Length(max = 30)
    private String color;
    
    @Column(name = "description")
    @Length(max = 300)
    private String description;
    
    @Column(name = "created_at") @NonNull
    private OffsetDateTime createdAt;
}
