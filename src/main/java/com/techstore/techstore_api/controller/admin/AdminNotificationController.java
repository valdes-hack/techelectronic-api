package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.AdminNotification;
import com.techstore.techstore_api.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationRepository notificationRepository;

    // 1. Voir les alertes non lues
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<AdminNotification>>> getUnread() {
        List<AdminNotification> list = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponse.<List<AdminNotification>>builder()
                .status("success").data(list).build());
    }

    // 2. Marquer une alerte comme lue (quand tu cliques dessus)
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        AdminNotification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));
        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok(ApiResponse.<Void>builder().status("success").build());
    }
}