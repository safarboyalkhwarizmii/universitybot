package com.example.repository;

import com.example.entity.CourseStudentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseStudentRepository extends CrudRepository<CourseStudentEntity, Long> {
    Optional<CourseStudentEntity> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<CourseStudentEntity> findByCourseId(Long courseId);

    List<CourseStudentEntity> findByStudentId(Long studentId);
}