package com.jaehyun.demo.core.repository.querydsl.impl;

import com.jaehyun.demo.core.entity.QReservation;
import com.jaehyun.demo.core.enums.ReservationStatus;
import com.jaehyun.demo.core.repository.querydsl.ReservationRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QReservation reservation = QReservation.reservation;

    @Override
    public Integer getSumVisitorCountWithLock(Long storeId, LocalDateTime start, LocalDateTime end) {

        return queryFactory
                .select(reservation.visitorCount.sum().coalesce(0))
                .from(reservation)
                .where(
                        reservation.store.id.eq(storeId),
                        reservation.status.in(ReservationStatus.PENDING, ReservationStatus.RESERVED),
                        reservation.reservedAt.before(end),
                        reservation.finishedAt.after(start)
                )
                //.setLockMode(LockModeType.PESSIMISTIC_WRITE) // 비관적 락은 reservation 데이터가 하나도 없을때 락이 걸리지 않음
                .fetchOne();
    }

}
