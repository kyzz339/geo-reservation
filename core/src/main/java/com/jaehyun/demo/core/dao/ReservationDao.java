package com.jaehyun.demo.core.dao;

import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Repository
public class ReservationDao {

    private final ReservationRepository reservationRepository;

    public Reservation saveReservation(Reservation reservation){
        return this.reservationRepository.save(reservation);
    }

    public List<Reservation> saveReservations(List<Reservation> reservations){
        return this.reservationRepository.saveAll(reservations);
    }

    public Integer getSumVisitorCountWithLock(Long storeId , LocalDateTime start , LocalDateTime end){
        return this.reservationRepository.getSumVisitorCountWithLock(storeId , start , end);
    }

    public Integer getSumVisitorCountExcludeMine(Long storeId , LocalDateTime start , LocalDateTime end , String email){
        return this.reservationRepository.getSumVisitorCountExcludeMine(storeId , start , end , email);
    }

    public List<Reservation> viewReservations(Long storeId){
        return this.reservationRepository.findByStoreIdOrderByReservedAt(storeId);
    }

    public List<Reservation> viewMyReservations(String email){
        return this.reservationRepository.findByUserEmail(email);
    }

    public Optional<Reservation> viewReservation(Long id){
        return this.reservationRepository.findById(id);
    }

    public Optional<Reservation> viewReservationWithLock(Long id){
        return this.reservationRepository.findWithLockById(id);
    }

    public void deleteAll(){
        this.reservationRepository.deleteAll();
    }


}
