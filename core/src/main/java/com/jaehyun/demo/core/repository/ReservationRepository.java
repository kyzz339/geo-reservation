package com.jaehyun.demo.core.repository;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.repository.querydsl.ReservationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReservationRepository extends JpaRepository<Reservation, Long> , ReservationRepositoryCustom {
}
