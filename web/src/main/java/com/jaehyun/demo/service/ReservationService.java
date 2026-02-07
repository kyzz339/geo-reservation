package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.ReservationDao;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.ReservationStatus;
import com.jaehyun.demo.dto.request.reservation.CreateReservationRequest;
import com.jaehyun.demo.dto.request.reservation.ReservationRequest;
import com.jaehyun.demo.dto.request.reservation.UpdateReservationRequest;
import com.jaehyun.demo.dto.response.reservation.CreateReservationResponse;
import com.jaehyun.demo.dto.response.reservation.ReservationResponse;
import com.jaehyun.demo.dto.response.reservation.UpdateReservationResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public CreateReservationResponse createReservation(CreateReservationRequest request , UserDetails userDetails){

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

        return CreateReservationResponse.from(savedReservation);

    }


    //사장님 본인가게 예약 확인 List
    public List<ReservationResponse> viewStoreReservation(ReservationRequest request , UserDetails userDetails){

        Store myStore = this.storeDao.getStore(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        if(!myStore.getOwner().getEmail().equals(userDetails.getUsername())){
            throw new SecurityException("본인 소유의 매장 예약만 조회할 수 있습니다.");
        }

        return this.reservationDao.viewReservations(request.getStoreId())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
    //손님 예약확인 List
    public List<ReservationResponse> viewMyReservation(UserDetails userDetails){

        return this.reservationDao.viewMyReservations(userDetails.getUsername())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
    //예약 취소 delete -> softdelete -> status : CANCELED 로 수정
    @Transactional
    public UpdateReservationResponse cancelReservation(UpdateReservationRequest request , UserDetails userDetails){

        Reservation savedReservation = this.reservationDao.viewReservation(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if(!userDetails.getUsername().equals(savedReservation.getUser().getEmail())){
            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
        }

        savedReservation.setStatus(ReservationStatus.CANCELED);
        savedReservation.setUpdatedAt(LocalDateTime.now());

        return UpdateReservationResponse.from(savedReservation);
    }

    //예약 변경
    @Transactional
    public UpdateReservationResponse changeReservation(UpdateReservationRequest request , UserDetails userDetails){

        Reservation savedReservation = this.reservationDao.viewReservation(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if(!userDetails.getUsername().equals(savedReservation.getUser().getEmail())){
            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
        }

        LocalDateTime start = request.getReservedAt();
        LocalDateTime end = Optional.ofNullable(request.getFinishedAt())
                .orElse(start.plusHours(1));

        Store savedStore = savedReservation.getStore();

        em.lock(savedStore , LockModeType.PESSIMISTIC_WRITE);
        //예약 가능 시간 체크
        Integer reservedCount = reservationDao.getSumVisitorCountExcludeMine(savedReservation.getStore().getId() , start , end , userDetails.getUsername());

        if(savedReservation.getVisitorCount() + reservedCount > savedReservation.getStore().getMaxCapacity()){
            throw new IllegalArgumentException("잔여 좌석이 부족합니다.");
        }
        //예약 변경
        savedReservation.setReservedAt(start);
        savedReservation.setFinishedAt(end);
        savedReservation.setVisitorCount(request.getVisitorCount());

        return UpdateReservationResponse.from(savedReservation);
    }

}
