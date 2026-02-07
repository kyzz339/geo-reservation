package com.jaehyun.demo.dto.request.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReservationRequest {

    private Long id;
    private Integer visitorCount;
    private LocalDateTime reservedAt;
    private LocalDateTime finishedAt;

}
