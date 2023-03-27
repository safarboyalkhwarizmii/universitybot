package com.example.service;

import com.example.entity.CourseEntity;
import com.example.repository.CourseRepository;
import com.example.repository.CourseStudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;

    private final CourseStudentService courseStudentService;

    public Long create(String courseName) {
        Optional<CourseEntity> byName = repository.findByName(courseName);
        if (!byName.isEmpty()) {
            return byName.get().getId();
        }

        CourseEntity course = new CourseEntity();
        course.setName(courseName);
        repository.save(course);
        return course.getId();
    }

    public Long getCount() {
        return repository.count();
    }

    public List<CourseEntity> findAll() {
        return (List<CourseEntity>) repository.findAll();
    }

    public Long getCourseIdByName(String courseName) {
        return repository.findByName(courseName).get().getId();
    }

    public CourseEntity findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Boolean deleteById(Long id) {
        Optional<CourseEntity> byId = repository.findById(id);
        if (byId.isEmpty()) {
            return false;
        }

        courseStudentService.deleteCourses(id);

        repository.delete(byId.get());
        return true;
    }
}
