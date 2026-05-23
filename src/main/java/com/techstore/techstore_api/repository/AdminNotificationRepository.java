package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {
    // Récupérer les alertes non lues, les plus récentes en premier
    List<AdminNotification> findByIsReadFalseOrderByCreatedAtDesc();
}