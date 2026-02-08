package com.jaehyun.demo.common.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ErrorResponse {
    private final int status;        // HTTP 상태 코드 (예: 400, 404)
    private final String code;       //
    private final String message;    // 사용자에게 보여줄 에러 메시지
    private final LocalDateTime timestamp; // 에러 발생 시간 (디버깅용)
}
