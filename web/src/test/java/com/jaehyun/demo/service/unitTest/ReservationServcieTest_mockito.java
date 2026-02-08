package com.jaehyun.demo.service.unitTest;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.common.exception.ErrorCode;
import com.jaehyun.demo.core.dao.ReservationDao;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.reservation.CreateReservationRequest;
import com.jaehyun.demo.dto.response.reservation.CreateReservationResponse;
import com.jaehyun.demo.service.ReservationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServcieTest_mockito {

    @Mock
    private EntityManager em;

    @Mock
    private UserDao userDao;
    @Mock
    private StoreDao storeDao;
    @Mock
    private ReservationDao reservationDao;

    @InjectMocks
    private ReservationService reservationService;

    @Spy
    private GeometryFactory geomereyFactory = new GeometryFactory(new PrecisionModel() , 4326);

    @Test
    @DisplayName("정상 예약 생성")
    void createReservationTest(){

        String email = "test@test.com";

        User existUser = User.builder()
                .id(1L)
                .email(email)
                .name("name")
                .type(Role.USER)
                .build();

        UserDetails userDetails = mock(UserDetails.class);

        Store existStore = Store.builder()
                        .id(1L)
                        .name("테스트 가게")
                        .description("테스트 가게입니다")
                        .location(geomereyFactory.createPoint(new Coordinate(17.0 , 18.0)))
                        .address("주소")
                        .maxCapacity(20)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end =start.plusHours(1);

        CreateReservationRequest request = CreateReservationRequest.builder()
                        .storeId(1L)
                        .visitorCount(5)
                        .reservedAt(start)
                        .finishedAt(end)
                        .build();

        Reservation mockReturn = Reservation.builder()
                .id(100L)
                .store(existStore)
                .user(existUser)
                .build();

        when(userDao.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(existUser));
        when(storeDao.getStore(1L)).thenReturn(Optional.of(existStore));
        when(reservationDao.getSumVisitorCountWithLock(existStore.getId() ,start ,end)).thenReturn(0);
        when(reservationDao.saveReservation(any(Reservation.class))).thenReturn(mockReturn);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.createReservation(request , userDetails);

        verify(reservationDao , times(1)).getSumVisitorCountWithLock(existStore.getId(), start , end);
        verify(reservationDao , times(1)).saveReservation(captor.capture());

        Reservation captureValue = captor.getValue();

        assertEquals(request.getVisitorCount() , captureValue.getVisitorCount());
        assertEquals(existStore , captureValue.getStore());
        assertEquals(existUser , captureValue.getUser());

    }

    @Test
    @DisplayName("maxCapacity 초과 예약")
    void createOverMaxCapacity(){

        String email = "test@test.com";

        User existUser = User.builder()
                .id(1L)
                .email(email)
                .name("name")
                .type(Role.USER)
                .build();

        UserDetails userDetails = mock(UserDetails.class);

        Store existStore = Store.builder()
                .id(1L)
                .name("테스트 가게")
                .description("테스트 가게입니다")
                .location(geomereyFactory.createPoint(new Coordinate(17.0 , 18.0)))
                .address("주소")
                .maxCapacity(10)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end =start.plusHours(1);

        CreateReservationRequest request = CreateReservationRequest.builder()
                .storeId(1L)
                .visitorCount(5)
                .reservedAt(start)
                .finishedAt(end)
                .build();

        when(userDao.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(existUser));
        when(storeDao.getStore(1L)).thenReturn(Optional.of(existStore));
        when(reservationDao.getSumVisitorCountWithLock(existStore.getId() ,start ,end)).thenReturn(6);

        CustomException exception = assertThrows(CustomException.class , () -> {
            reservationService.createReservation(request , userDetails);
        });

        assertTrue(exception.getMessage().contains("잔여 좌석이 부족합니다."));
        assertEquals(exception.getErrorCode() , ErrorCode.CAPACITY_EXCEEDED);
    }
}
