package com.example.repository;

import com.example.entity.StudentEntity;
import com.example.entity.UserHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<StudentEntity, Long> {

}
