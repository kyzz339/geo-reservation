package com.jaehyun.demo.core.dao;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Log4j2
@RequiredArgsConstructor
@Repository
public class ReservationDao {

    private final ReservationRepository reservationRepository;

    public Reservation saveReservation(Reservation reservation){
        return this.reservationRepository.save(reservation);
    }


}
