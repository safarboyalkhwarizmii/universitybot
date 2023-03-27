package com.example.service;

import com.example.entity.CourseStudentEntity;
import com.example.repository.CourseStudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CourseStudentService {
    private final CourseStudentRepository repository;

    public Long create(Long studentId, Long courseId) {
        Optional<CourseStudentEntity> byCourseIdAndStudentId = repository.findByCourseIdAndStudentId(courseId, studentId);
        if (!byCourseIdAndStudentId.isEmpty()) {
            return byCourseIdAndStudentId.get().getId();
        }

        CourseStudentEntity courseStudent = new CourseStudentEntity();
        courseStudent.setStudentId(studentId);
        courseStudent.setCourseId(courseId);
        repository.save(courseStudent);
        return courseStudent.getId();
    }

    public List<CourseStudentEntity> getCourseInfo(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    public void delete(Long studentId, Long courseId) {
        Optional<CourseStudentEntity> byCourseIdAndStudentId = repository.findByCourseIdAndStudentId(courseId, studentId);
        if (byCourseIdAndStudentId.isEmpty()) {
            return;
        }

        CourseStudentEntity courseStudentEntity = byCourseIdAndStudentId.get();
        repository.delete(courseStudentEntity);
    }

    public void deleteStudentFromCourses(Long studentId) {
        List<CourseStudentEntity> byStudentId = repository.findByStudentId(studentId);
        for (CourseStudentEntity courseStudentEntity : byStudentId) {
            repository.delete(courseStudentEntity);
        }
    }

    public void deleteCourses(Long courseId) {
        List<CourseStudentEntity> byStudentId = repository.findByCourseId(courseId);
        for (CourseStudentEntity courseStudentEntity : byStudentId) {
            repository.delete(courseStudentEntity);
        }
    }
}
