package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagsRepository extends JpaRepository<Tag, String> {
    boolean existsByName(String name);
    boolean existsByNameAndTagIdNot(String name, String id);
}
