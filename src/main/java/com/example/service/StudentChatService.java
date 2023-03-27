package com.example.service;

import com.example.entity.StudentChatEntity;
import com.example.repository.StudentChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentChatService {
    private final StudentChatRepository repository;

    public Long create(Long studentId, Long chatId) {
        StudentChatEntity studentChat = new StudentChatEntity();
        studentChat.setChatId(chatId);
        studentChat.setStudentId(studentId);
        repository.save(studentChat);

        return studentChat.getChatId();
    }
}