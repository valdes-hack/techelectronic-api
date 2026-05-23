package com.techstore.techstore_api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private LocalDateTime timestamp;
    private T data;
}