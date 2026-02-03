package com.jaehyun.demo.dto.response.reservation;

import com.jaehyun.demo.core.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private String storeName;
    private String reserveName;
    private LocalDateTime reservedAt;
    private LocalDateTime finishedAt;

    public static ReservationResponse from(Reservation reservation){
        return ReservationResponse.builder()
                .storeName(reservation.getStore().getName())
                .reserveName(reservation.getUser().getName())
                .reservedAt(reservation.getReservedAt())
                .finishedAt(reservation.getFinishedAt())
                .build();
    }

}
