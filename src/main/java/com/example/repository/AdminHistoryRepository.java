package com.example.repository;

import com.example.entity.AdminHistoryEntity;
import com.example.enums.AdminStep;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminHistoryRepository extends CrudRepository<AdminHistoryEntity, Long> {
    Optional<AdminHistoryEntity> findTop1ByAdminIdOrderByIdDesc(Long adminId);

    Optional<AdminHistoryEntity> findTop1ByAdminIdAndStepOrderByIdDesc(Long adminId, AdminStep step);

    @Query(value = "select * from admin_history where admin_id=?1 and step like ?2 order by id desc limit 1", nativeQuery = true)
    Optional<AdminHistoryEntity> findTop1ByAdminIdAndStepLike(Long adminId, String step);
}
