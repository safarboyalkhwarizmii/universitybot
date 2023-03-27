package com.example.service;

import com.example.entity.AdminHistoryEntity;
import com.example.enums.AdminStep;
import com.example.repository.AdminHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@AllArgsConstructor
public class AdminHistoryService {
    private final AdminHistoryRepository repository;

    public Long create(Long userId, AdminStep step, String value) {
        AdminHistoryEntity userHistory = new AdminHistoryEntity();
        userHistory.setAdminId(userId);
        userHistory.setStep(step);
        userHistory.setValue(value);
        repository.save(userHistory);
        return userHistory.getId();
    }

    public AdminStep getLastStepByAdminId(Long adminId) {
        try {
            return repository.findTop1ByAdminIdOrderByIdDesc(adminId).get().getStep();
        } catch (NoSuchElementException e) {

        }

        return null;
    }

    public String getLastValueByStep(Long userId, AdminStep adminStep) {
        return repository.findTop1ByAdminIdAndStepOrderByIdDesc(userId, adminStep).get().getValue();
    }

    public AdminStep getLastOpenedStep(Long userId) {
        return repository.findTop1ByAdminIdAndStepLike(userId, "%_OPENED").get().getStep();
    }
}
