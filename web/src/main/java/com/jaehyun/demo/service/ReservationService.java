package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.ReservationDao;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.dto.request.reservation.ReservationRequest;
import com.jaehyun.demo.dto.response.reservation.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.jaehyun.demo.core.enums.ReservationStatus.PENDING;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UserDao userDao;
    private final StoreDao storeDao;
    private final ReservationDao reservationDao;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request , UserDetails userDetails){

        User user = userDao.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다 userId : " + userDetails.getUsername()));

        Store existStore = storeDao.getStore(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장이 존재하지 않습니다. storeId : " + request.getStoreId()));

        LocalDateTime start = request.getReservedAt();
        LocalDateTime end = Optional.ofNullable(request.getFinishedAt())
                .orElse(start.plusHours(1));

        //동시성 제어 -> 현재 예약된 인원 확인(비관적 락 적용 예정)

        Reservation reservation = Reservation.builder()
                .user(user)
                .store(existStore)
                .visitorCount(request.getVisitorCount())
                .reservedAt(start)
                .finishedAt(end)
                .status(PENDING)
                .build();

        Reservation savedReservation = reservationDao.saveReservation(reservation);

        return ReservationResponse.from(savedReservation);

    }

}
