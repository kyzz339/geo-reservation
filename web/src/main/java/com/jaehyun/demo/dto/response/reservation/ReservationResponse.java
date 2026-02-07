package com.jaehyun.demo.dto.response.reservation;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.enums.ReservationStatus;
import com.jaehyun.demo.dto.response.store.StoreResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {

    private Long id;
    private String userName;
    private String userEmail;
    private Integer visitorCount;
    private LocalDateTime reservedAt;
    private LocalDateTime finishedAt;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private StoreResponse storeResponse;

    public static ReservationResponse from(Reservation reservation){
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userName(reservation.getUser().getName())
                .userEmail(reservation.getUser().getEmail())
                .visitorCount(reservation.getVisitorCount())
                .reservedAt(reservation.getReservedAt())
                .finishedAt(reservation.getFinishedAt())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    public static ReservationResponse fromWithStore(Reservation reservation){
        return ReservationResponse.builder()
                .storeResponse(StoreResponse.from(reservation.getStore()))
                .id(reservation.getId())
                .userName(reservation.getUser().getName())
                .userEmail(reservation.getUser().getEmail())
                .visitorCount(reservation.getVisitorCount())
                .reservedAt(reservation.getReservedAt())
                .finishedAt(reservation.getFinishedAt())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

}
