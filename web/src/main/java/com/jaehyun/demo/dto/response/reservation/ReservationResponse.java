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
    private String userName; // 예약자명
    private String userEmail;
    private String storeName; // 가게 이름 추가
    private Long storeId;
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
                .storeName(reservation.getStore().getName()) // 가게 이름 설정
                .storeId(reservation.getStore().getId())
                .visitorCount(reservation.getVisitorCount())
                .reservedAt(reservation.getReservedAt())
                .finishedAt(reservation.getFinishedAt())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    public static ReservationResponse fromWithStore(Reservation reservation){
        ReservationResponse response = from(reservation);
        response.setStoreResponse(StoreResponse.from(reservation.getStore()));
        return response;
    }

}
