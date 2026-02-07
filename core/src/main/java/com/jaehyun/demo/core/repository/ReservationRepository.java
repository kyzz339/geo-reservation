package com.jaehyun.demo.core.repository;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.repository.querydsl.ReservationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReservationRepository extends JpaRepository<Reservation, Long> , ReservationRepositoryCustom {

    List<Reservation> findByStoreIdOrderByReservedAt(Long storeId);

    List<Reservation> findByUserEmail(String email);
}
