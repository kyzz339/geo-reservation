package com.jaehyun.demo.core.repository;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.repository.querydsl.ReservationRepositoryCustom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;


public interface ReservationRepository extends JpaRepository<Reservation, Long> , ReservationRepositoryCustom {

    List<Reservation> findByStoreIdOrderByReservedAt(Long storeId);

    List<Reservation> findByUserEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findWithLockById(Long id);
}
