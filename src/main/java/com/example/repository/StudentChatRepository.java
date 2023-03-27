package com.example.repository;

import com.example.entity.StudentChatEntity;
import com.example.entity.UserHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentChatRepository extends CrudRepository<StudentChatEntity, Long> {

}
