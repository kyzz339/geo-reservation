package com.jaehyun.demo.service.integrationTest;

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
import com.jaehyun.demo.dto.response.reservation.CreateReservationResponse;
import com.jaehyun.demo.dto.response.reservation.ReservationResponse;
import com.jaehyun.demo.service.ReservationService;
import com.jaehyun.demo.service.integrationTest.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReservationServiceTest_integrationTest extends IntegrationTestSupport {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    @DisplayName("예약 생성 - 성공")
    void createReservation_success(){

        String ownerEmail = "owner@test.com";

        User owner = createTestOwner(ownerEmail);

        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime reserveTime = LocalDateTime.now();

        CreateReservationRequest request = CreateReservationRequest.builder()
                .storeId(store.getId())
                .visitorCount(5)
                .reservedAt(reserveTime)
                .finishedAt(reserveTime.plusHours(1))
                .build();

        String userEmail = "user@test.com";

        User user = createTestUser(userEmail);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userEmail)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        CreateReservationResponse response = reservationService.createReservation(request , userDetails);

        assertThat(response.getReserveName()).isEqualTo(user.getName());
        assertThat(response.getStoreName()).isEqualTo(store.getName());

        //디비값 추가 확인
        Reservation savedReservation = reservationDao.viewReservation(response.getReservationId()).orElseThrow();

        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(savedReservation.getVisitorCount()).isEqualTo(request.getVisitorCount());
        assertThat(savedReservation.getUser().getEmail()).isEqualTo(userEmail);
        assertThat(savedReservation.getStore().getId()).isEqualTo(store.getId());

    }

    @Test
    @DisplayName("예약 실패 - 수용인원 초과")
    void createReservation_fail_overCapacity(){
        String ownerEmail = "owner@test.com";

        User owner = createTestOwner(ownerEmail);

        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime reserveTime = LocalDateTime.now();

        CreateReservationRequest request = CreateReservationRequest.builder()
                .storeId(store.getId())
                .visitorCount(21)
                .reservedAt(reserveTime)
                .finishedAt(reserveTime.plusHours(1))
                .build();

        String userEmail = "user@test.com";

        User user = createTestUser(userEmail);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userEmail)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        CustomException exception = assertThrows(CustomException.class, () ->{
           reservationService.createReservation(request , userDetails);
        });

        assertThat(exception.getMessage()).contains("잔여 좌석이 부족합니다.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAPACITY_EXCEEDED);

    }

    @Test
    @DisplayName("본인 가게 예약 확인 성공")
    void viewStoreReservation_success(){

        //가게 생성
        String ownerEmail = "owner@test.com";
        User owner = createTestOwner(ownerEmail);
        Store store = createTestStore("테스트 가게" , owner);

        UserDetails ownerDetails = org.springframework.security.core.userdetails.User.builder()
                .username(ownerEmail)
                .password("password")
                .authorities("ROLE_OWNER")
                .build();

        String otherEmail = "other@test.com";
        User otherOwner = createTestOwner(otherEmail);
        Store otherStore = createTestStore("다른 가게" , otherOwner);

        //예약 생성
        String userEmail = "user@test.com";

        User user = createTestUser(userEmail);

        List<Reservation> reservations = Arrays.asList(
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(otherStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .build()
        );

            reservationDao.saveReservations(reservations);

        ReservationRequest request = ReservationRequest.builder()
                .storeId(store.getId())
                .build();

        List<ReservationResponse> myReservations = reservationService.viewStoreReservation(request , ownerDetails);

        assertThat(myReservations).isNotNull();
        assertThat(myReservations.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("손님 - 예약확인")
    void viewStoreReservation(){

        //가게 생성
        String ownerEmail = "owner@test.com";
        User owner = createTestOwner(ownerEmail);
        Store store = createTestStore("테스트 가게" , owner);

        String otherEmail = "other@test.com";
        User otherOwner = createTestOwner(otherEmail);
        Store otherStore = createTestStore("다른 가게" , otherOwner);

        //예약 생성
        String userEmail = "user@test.com";

        User user = createTestUser(userEmail);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userEmail)
                .password("password")
                .authorities("ROLE_OWNER")
                .build();

        List<Reservation> reservations = Arrays.asList(
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(otherStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.now())
                        .finishedAt(LocalDateTime.now().plusHours(1))
                        .status(ReservationStatus.PENDING)
                        .build()
        );

        reservationDao.saveReservations(reservations);

        List<ReservationResponse> myReservation = reservationService.viewMyReservation(userDetails);

        assertThat(myReservation).isNotNull();
        assertThat(myReservation.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("예약 취소 - 성공 (상태값이 CANCELED로 변경되어야 함)")
    void cancelReservation_success() {
        User user = createTestUser("canceler@test.com");
        Store store = createTestStore("취소매장", createTestOwner("owner2@test.com"));
        Reservation reservation = reservationDao.saveReservation(createReservation(user, store, ReservationStatus.PENDING));

        Long reservationId = reservation.getId();

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password("password").authorities("ROLE_USER").build();

        reservationService.cancelReservation(reservationId, userDetails);

        Reservation result = reservationDao.viewReservation(reservation.getId()).get();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }



    private Reservation createReservation(User user, Store store , ReservationStatus status) {
        return Reservation.builder()
                .user(user)
                .store(store)
                .visitorCount(5)
                .reservedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusHours(1))
                .status(status)
                .build();
    }

    private User createTestUser(String email){
        User user = User.builder()
                .email(email)
                .name("고객이름")
                .password("password")
                .type(Role.USER)
                .build();

        userDao.save(user);

        return user;
    }

    private User createTestOwner(String email){
        User user = User.builder()
                .email(email)
                .name("주인이름")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(user);

        return user;
    }

    private Store createTestStore(String storeName , User owner){

        Store store = Store.builder()
                .name(storeName)
                .owner(owner)
                .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                .address("서울시")
                .maxCapacity(20)
                .deleted(false)
                .build();

        storeDao.saveStore(store);

        return store;
    }

}
