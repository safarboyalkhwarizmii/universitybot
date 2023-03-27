package com.example.service;

import com.example.entity.UserHistoryEntity;
import com.example.enums.UserStep;
import com.example.repository.UserHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserHistoryService {
    private final UserHistoryRepository repository;

    public Long create(Long userId, UserStep step, String value) {
        UserHistoryEntity userHistory = new UserHistoryEntity();
        userHistory.setUserId(userId);
        userHistory.setUserStep(step);
        userHistory.setValue(value);
        repository.save(userHistory);
        return userHistory.getId();
    }

    public UserStep getLastStepByUserId(Long userId) {
        return repository.findTop1ByUserId(userId).get().getUserStep();
    }

    public String getLastValueByStep(Long userId, UserStep userStep) {
        return repository.findTop1ByUserIdAndUserStep(userId, userStep).get().getValue();
    }
}
