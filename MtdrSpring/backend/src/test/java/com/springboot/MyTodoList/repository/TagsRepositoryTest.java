package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TagsRepositoryTest {

    @Test
    void repositoryExtendsJpaRepositoryForTagIds() {
        assertThat(JpaRepository.class).isAssignableFrom(TagsRepository.class);
    }

    @Test
    void existsByNameDerivedQueryContractUsesStringAndBoolean() throws Exception {
        Method method = TagsRepository.class.getMethod("existsByName", String.class);

        assertThat(method.getReturnType()).isEqualTo(boolean.class);
        assertThat(method.getParameterTypes()).containsExactly(String.class);
    }

    @Test
    void tagEntityMapsRepositoryIdentifierAsString() throws Exception {
        assertThat(Tag.class.getDeclaredField("tagId").getType()).isEqualTo(String.class);
    }
}
