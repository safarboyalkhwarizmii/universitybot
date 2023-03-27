package com.example.repository;

import com.example.entity.CourseEntity;
import com.example.entity.UserHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends CrudRepository<CourseEntity, Long> {
    Optional<CourseEntity> findByName(String name);
}
