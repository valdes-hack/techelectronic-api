package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.AdminNotification;
import com.techstore.techstore_api.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    // 1b. Voir TOUT l'historique des notifications (paginé)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminNotification>>> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminNotification> history = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<AdminNotification>>builder()
                .status("success").data(history).build());
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

    // 3. Marquer tout comme lu
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        List<AdminNotification> unreadList = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unreadList.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadList);
        return ResponseEntity.ok(ApiResponse.<Void>builder().status("success").build());
    }
}