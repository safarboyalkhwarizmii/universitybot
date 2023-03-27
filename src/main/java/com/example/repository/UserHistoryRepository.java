package com.example.repository;

import com.example.entity.UserHistoryEntity;
import com.example.enums.UserStep;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserHistoryRepository extends CrudRepository<UserHistoryEntity, Long> {
    Optional<UserHistoryEntity> findTop1ByUserId(Long userId);

    Optional<UserHistoryEntity> findTop1ByUserIdAndUserStep(Long userId, UserStep step);
}
