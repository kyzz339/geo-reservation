package com.jaehyun.demo.service.unitTest;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.common.exception.ErrorCode;
import com.jaehyun.demo.core.dao.ReservationDao;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Reservation;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.ReservationStatus;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.reservation.CreateReservationRequest;
import com.jaehyun.demo.dto.request.reservation.ReservationRequest;
import com.jaehyun.demo.dto.request.reservation.UpdateReservationRequest;
import com.jaehyun.demo.dto.response.reservation.ReservationResponse;
import com.jaehyun.demo.dto.response.reservation.UpdateReservationResponse;
import com.jaehyun.demo.service.ReservationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

    private final GeometryFactory geomereyFactory = new GeometryFactory(new PrecisionModel() , 4326);

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

    //본인 가게 리스트 확인 - 가게주인
    @Test
    @DisplayName("본인 가게 리스트 확인")
    void displayMyStore(){

        String email = "test@test.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .name("name")
                .type(Role.OWNER)
                .build();

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(email);

        Store existStore = Store.builder()
                .id(1L)
                .name("테스트 가게")
                .description("테스트 가게입니다")
                .location(geomereyFactory.createPoint(new Coordinate(17.0 , 18.0)))
                .address("주소")
                .maxCapacity(10)
                .active(true)
                .createdAt(LocalDateTime.now())
                .owner(user)
                .build();

        List<Reservation> reservations = Arrays.asList(
                Reservation.builder()
                        .id(1L)
                        .user(user)
                        .store(existStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),

                Reservation.builder()
                        .id(2L)
                        .user(user)
                        .store(existStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now().plusDays(1))
                        .finishedAt(LocalDateTime.now().plusDays(1).plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        ReservationRequest request = ReservationRequest.builder()
                        .storeId(1L)
                        .build();


        when(storeDao.getStore(1L)).thenReturn(Optional.of(existStore));
        when(reservationDao.viewReservations(1L)).thenReturn(reservations);

        List<ReservationResponse> response =  reservationService.viewStoreReservation(request , userDetails);

        verify(storeDao , times(1)).getStore(1L);
        verify(reservationDao , times(1)).viewReservations(1L);

        assertNotNull(reservations);
        assertEquals(1L , response.get(0).getId());
        assertEquals(2L , response.get(1).getId());
    }
    //손님 예약 확인 list
    @Test
    @DisplayName("손님-예약확인 List")
    void viewMyReservation(){

        String email = "test@test.com";

        User existCustomer = User.builder()
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

        List<Reservation> reservations = Arrays.asList(
                Reservation.builder()
                        .id(1L)
                        .user(existCustomer)
                        .store(existStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),

                Reservation.builder()
                        .id(2L)
                        .user(existCustomer)
                        .store(existStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now().plusDays(1))
                        .finishedAt(LocalDateTime.now().plusDays(1).plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        when(userDetails.getUsername()).thenReturn(email);
        when(reservationDao.viewMyReservations(email)).thenReturn(reservations);

        List<ReservationResponse> response = reservationService.viewMyReservation(userDetails);

        verify(reservationDao , times(1)).viewMyReservations(email);

        assertEquals(1L , response.get(0).getId());
        assertEquals(2L , response.get(1).getId());

    }
    //예약 취소
    @Test
    @DisplayName("예약 취소")
    void cancelReservation(){
        String email = "test@test.com";

        User existCustomer = User.builder()
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

        Reservation exsistReservation =  Reservation.builder()
                .id(1L)
                .user(existCustomer)
                .store(existStore)
                .visitorCount(5)
                .reservedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusHours(1))
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reservationDao.viewReservation(1L)).thenReturn(Optional.of(exsistReservation));
        when(userDetails.getUsername()).thenReturn(email);

        UpdateReservationRequest request = UpdateReservationRequest.builder()
                .id(1L)
                .build();


        UpdateReservationResponse response = reservationService.cancelReservation(request , userDetails);

        verify(reservationDao , times(1)).viewReservation(1L);

        assertEquals(response.getId() , request.getId());
        assertEquals(response.getStatus() , ReservationStatus.CANCELED);
    }
    //예약 변경
    @Test
    @DisplayName("예약 변경")
    void changeReservation(){

        String email = "test@test.com";

        User existCustomer = User.builder()
                .id(1L)
                .email(email)
                .name("name")
                .type(Role.USER)
                .build();

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

        Reservation exsistReservation =  Reservation.builder()
                .id(1L)
                .user(existCustomer)
                .store(existStore)
                .visitorCount(5)
                .reservedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusHours(1))
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserDetails userDetails = mock(UserDetails.class);

        when(reservationDao.viewReservation(1L)).thenReturn(Optional.of(exsistReservation));
        when(userDetails.getUsername()).thenReturn(email);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        when(reservationDao.getSumVisitorCountExcludeMine(1L , start , end , email)).thenReturn(5);

        UpdateReservationRequest request = UpdateReservationRequest.builder()
                .id(1L)
                .reservedAt(start)
                .finishedAt(end)
                .visitorCount(3)
                .build();

        UpdateReservationResponse response = reservationService.changeReservation(request , userDetails);

        verify(reservationDao , times(1)).viewReservation(1L);

        assertEquals(response.getId() , request.getId());
        assertEquals(response.getStatus() , ReservationStatus.PENDING);
    }
}
