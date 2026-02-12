package com.jaehyun.demo.dto.response.reservation;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReservationResponse {

    private Long id;
    private ReservationStatus status;

    public static UpdateReservationResponse from(Reservation reservation){
        return UpdateReservationResponse.builder()
                .id(reservation.getId())
                .status(reservation.getStatus())
                .build();
    }

}
