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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReservationServiceTest_integrationTest extends IntegrationTestSupport {

    @Autowired
    private EntityManager em;

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

    @BeforeEach
    void setUp() {
        reservationDao.deleteAll();
        storeDao.deleteAll();
        userDao.deleteAll();

        if (TestTransaction.isActive()) {
            em.flush();
            em.clear();
        }
    }

    @Test
    @DisplayName("예약 생성 - 성공")
    void createReservation_success(){

        String ownerEmail = "owner@test.com";

        User owner = createTestOwner(ownerEmail);

        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime reserveTime = LocalDateTime.of(2026, 2, 28, 14, 0); // Open time is 09:00

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
    @DisplayName("예약 생성 - 실패 (영업시간 아님)")
    void createReservation_fail_invalidTime(){

        String ownerEmail = "owner@test.com";
        User owner = createTestOwner(ownerEmail);
        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime reserveTime = LocalDateTime.of(2026, 2, 28, 8, 0); // Open time is 09:00

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

        CustomException exception = assertThrows(CustomException.class, () ->{
            reservationService.createReservation(request , userDetails);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_RESERVATION_TIME);
    }

    @Test
    @DisplayName("예약 실패 - 수용인원 초과")
    void createReservation_fail_overCapacity(){
        String ownerEmail = "owner@test.com";

        User owner = createTestOwner(ownerEmail);

        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime reserveTime = LocalDateTime.of(2026, 2, 28, 14, 0);

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
                        .reservedAt(LocalDateTime.of(2026, 2, 28, 10, 0))
                        .finishedAt(LocalDateTime.of(2026, 2, 28, 11, 0))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(otherStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.of(2026, 2, 28, 10, 0))
                        .finishedAt(LocalDateTime.of(2026, 2, 28, 11, 0))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.of(2026, 2, 28, 12, 0))
                        .finishedAt(LocalDateTime.of(2026, 2, 28, 13, 0))
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
                        .reservedAt(LocalDateTime.of(2026, 2, 28, 10, 0))
                        .finishedAt(LocalDateTime.of(2026, 2, 28, 11, 0))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(otherStore)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.of(2026, 2, 28, 10, 0))
                        .finishedAt(LocalDateTime.of(2026, 2, 28, 11, 0))
                        .status(ReservationStatus.PENDING)
                        .build(),
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .visitorCount(5)
                        .reservedAt(LocalDateTime.of(2026, 2, 28, 12, 0))
                        .finishedAt(LocalDateTime.of(2026, 2, 28, 13, 0))
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

    @Test
    @DisplayName("동시성 테스트 createReservation")
    void concurrntCreateReservation() throws InterruptedException{

        User owner = createTestUser("owner@test.com");
        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime startTime = LocalDateTime.of(2026, 2, 28, 14, 0);
        LocalDateTime endTime = startTime.plusHours(1);

        int threadCount = 100; //가상 유저 수
        List<User> testUsers = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            testUsers.add(createTestUser(i + "user@test.com"));
        }

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start(); // 계정 미리 commit

        ExecutorService executorService = Executors.newFixedThreadPool(32); // 스레드 풀 -> 스레드 관리
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for(int i=0; i<threadCount; i++){

            User user = testUsers.get(i);

            UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                    .password("password").authorities("ROLE_USER").build();

            CreateReservationRequest createRequest = CreateReservationRequest.builder()
                            .storeId(store.getId())
                            .visitorCount(1)
                            .reservedAt(startTime)
                            .finishedAt(endTime)
                            .build();

            executorService.submit(() ->{
                try {
                    reservationService.createReservation(createRequest, userDetails);
                    successCount.incrementAndGet();
                }catch (Exception e){
                    failCount.incrementAndGet();
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        System.out.println("최종 결과 -> 성공: " + successCount.get() + ", 실패: " + failCount.get());
        assertThat(successCount.get()).isEqualTo(store.getMaxCapacity());

    }

    @Test
    @DisplayName("동시성 테스트 updateReservation")
    void concurrtUpdateReservation() throws InterruptedException{

        User owner = createTestUser("owner@test.com");
        Store store = createTestStore("테스트 가게" , owner);

        LocalDateTime startTime = LocalDateTime.of(2026, 2, 28, 10, 0);
        LocalDateTime endTime = startTime.plusHours(1);

        int threadCount = 20;

        List<User> testUsers = new ArrayList<>();
        List<Long> reservationIds = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            User user = createTestUser(i + "update_user@test.com");
            testUsers.add(user);

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .store(store)
                    .visitorCount(1)
                    .reservedAt(startTime)
                    .finishedAt(endTime)
                    .status(ReservationStatus.PENDING)
                    .build();

            Reservation saved = reservationDao.saveReservation(reservation);
            reservationIds.add(saved.getId());
        }

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for(int i=0; i<threadCount; i++){
            int index= i;
            User user = testUsers.get(index);
            Long resId = reservationIds.get(index);

            UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                    .password("password").authorities("ROLE_USER").build();

            UpdateReservationRequest updateRequest = UpdateReservationRequest.builder()
                    .id(resId)
                    .visitorCount(2)
                    .reservedAt(startTime)
                    .finishedAt(endTime)
                    .build();

            executorService.submit(() ->{
               try{
                   reservationService.changeReservation(updateRequest, userDetails);
                   successCount.incrementAndGet();
               } catch (Exception e){
                   failCount.incrementAndGet();
               }finally {
                   latch.countDown();
               }
            });
        }
        latch.await();
        executorService.shutdown();

        System.out.println("최종 결과 -> 변경 성공: " + successCount.get() + ", 변경 실패: " + failCount.get());
        assertThat(successCount.get()).isEqualTo(0);

    }


    private Reservation createReservation(User user, Store store , ReservationStatus status) {
        return Reservation.builder()
                .user(user)
                .store(store)
                .visitorCount(5)
                .reservedAt(LocalDateTime.of(2026, 2, 28, 14, 0))
                .finishedAt(LocalDateTime.of(2026, 2, 28, 15, 0))
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
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .deleted(false)
                .build();

        storeDao.saveStore(store);

        return store;
    }

}