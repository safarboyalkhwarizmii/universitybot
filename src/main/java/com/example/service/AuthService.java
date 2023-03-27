package com.example.service;

import com.example.entity.StudentEntity;
import com.example.repository.StudentRepository;
import com.example.utils.MD5;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class AuthService {
    private final StudentRepository studentRepository;

    public Boolean login(Long studentId, String password) {
        Optional<StudentEntity> byId = studentRepository.findById(studentId);
        return byId.get().getPassword().equals(MD5.encode(password));
    }
}
