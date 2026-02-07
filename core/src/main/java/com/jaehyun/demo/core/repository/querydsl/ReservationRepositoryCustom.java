package com.jaehyun.demo.core.repository.querydsl;

import java.time.LocalDateTime;

public interface ReservationRepositoryCustom {

    Integer getSumVisitorCountWithLock(Long storeId , LocalDateTime start , LocalDateTime end);

    Integer getSumVisitorCountExcludeMine(Long storeId , LocalDateTime start , LocalDateTime end , String email);

}
