package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.ReservationDao;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.dto.request.reservation.ReservationRequest;
import com.jaehyun.demo.dto.response.reservation.ReservationResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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

    private final EntityManager em;

    private final UserDao userDao;
    private final StoreDao storeDao;
    private final ReservationDao reservationDao;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request , UserDetails userDetails){

        User user = userDao.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다 userId : " + userDetails.getUsername()));

        Store existStore = storeDao.getStore(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장이 존재하지 않습니다. storeId : " + request.getStoreId()));

        em.lock(existStore , LockModeType.PESSIMISTIC_WRITE); // 예약 인원이 0일경우 락이 걸리지 않기 떄문에 store은 무조건 존재하기 떄문에 lock, 해당 트랜잭션이 끝날때까지 lock이 걸림

        LocalDateTime start = request.getReservedAt();
        LocalDateTime end = Optional.ofNullable(request.getFinishedAt())
                .orElse(start.plusHours(1));

        //동시성 제어 -> 현재 예약된 인원 확인 -> 여기서 lock 걸면 데이터가 하나도 없을떄 lock 이 안걸림
        Integer reservedCount = reservationDao.getSumVisitorCountWithLock(existStore.getId() , start , end); // 예약된 숫자

        if(request.getVisitorCount() + reservedCount > existStore.getMaxCapacity()){
            throw new IllegalArgumentException("잔여 좌석이 부족합니다.");
        }

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

    //사장님 본인가게 예약 확인 List
    //손님 예약확인 List
    //예약 취소 delete -> softdelete
    //예약 변경

}
