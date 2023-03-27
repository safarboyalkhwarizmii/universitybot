package com.example.service;

import com.example.entity.StudentEntity;
import com.example.repository.StudentRepository;
import com.example.utils.MD5;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentService {
    private final StudentRepository repository;
    private final CourseStudentService courseStudentService;

    public Long create(String firstName, String lastName, String password) {
        StudentEntity student = new StudentEntity();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setPassword(MD5.encode(password));
        repository.save(student);
        return student.getId();
    }

    public Boolean delete(Long id) {
        Optional<StudentEntity> byId = repository.findById(id);
        if (byId.isEmpty()) {
            return false;
        }

        courseStudentService.deleteStudentFromCourses(id);
        repository.delete(byId.get());
        return true;
    }

    public StudentEntity findById(Long id) {
        Optional<StudentEntity> byId = repository.findById(id);
        return byId.orElse(null);
    }

    public Long getCount() {
        return repository.count();
    }
}