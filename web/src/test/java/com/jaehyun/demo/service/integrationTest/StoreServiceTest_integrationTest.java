package com.jaehyun.demo.service.integrationTest;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.common.exception.ErrorCode;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.request.store.LocationRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import com.jaehyun.demo.dto.response.store.DeleteStoreResponse;
import com.jaehyun.demo.dto.response.store.StoreResponse;
import com.jaehyun.demo.service.StoreService;
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

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StoreServiceTest_integrationTest extends IntegrationTestSupport {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    @DisplayName("매장 생성 통합 테스트")
    void createStoreIntegrationTest(){

        String email = "test@test.com";
        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("password")
                .authorities("ROLE_OWNER")
                .build();

        CreateStoreRequest createStoreRequest = CreateStoreRequest.builder()
                .name("테스트 가게 매장")
                .description("테스트 가게 설명")
                .longitude(127.0560)
                .latitude(37.5446)
                .address("서울시 성동구 성수동")
                .maxCapacity(30)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        CreateStoreResponse createStoreReseponse = storeService.createStore(createStoreRequest , userDetails);

        assertThat(createStoreReseponse.getId()).isNotNull();
        assertThat(createStoreReseponse.getName()).isEqualTo("테스트 가게 매장");

        Store savedStore = storeDao.getStore(createStoreReseponse.getId()).orElseThrow();

        assertThat(savedStore.getOwner().getEmail()).isEqualTo(email);
        assertThat(savedStore.getLocation()).isNotNull();
        assertThat(savedStore.getLocation().getX()).isEqualTo(127.0560);
        assertThat(savedStore.getLocation().getY()).isEqualTo(37.5446);
    }

    @Test
    @DisplayName("매장 삭제 - 성공")
    void deleteStore(){
        String email = "test@test.com";

        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("password")
                .authorities("ROLE_OWNER")
                .build();

        Store store = Store.builder()
                .name("테스트 가게")
                .owner(owner)
                .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                .address("서울시")
                .maxCapacity(20)
                .deleted(false)
                .build();

        Store savedStore = storeDao.saveStore(store);

        DeleteStoreResponse deleteStoreResponse = storeService.deleteStore(savedStore.getId() , userDetails);

        assertThat(deleteStoreResponse.getId()).isEqualTo(savedStore.getId());
        assertThat(deleteStoreResponse.getName()).isEqualTo(savedStore.getName());

        Store deletedStore = storeDao.getStore(savedStore.getId()).orElseThrow();

        assertThat(deletedStore.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("매장 삭제(실패) - 다른 계정 삭제시도")
    void deleteStore_fail(){
        String email = "test@test.com";

        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        User other_owner = User.builder()
                .email("1" + email)
                .name("다른사장님")
                .password("1password")
                .type(Role.OWNER)
                .build();

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(other_owner.getEmail())
                .password("1password")
                .authorities("ROLE_OWNER")
                .build();

        Store store = Store.builder()
                .name("테스트 가게")
                .owner(owner)
                .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                .address("서울시")
                .maxCapacity(20)
                .deleted(false)
                .build();

        Store savedStore = storeDao.saveStore(store);

        CustomException exception = assertThrows(CustomException.class,() -> {
            storeService.deleteStore(savedStore.getId() , userDetails);
        });

        assertThat(exception.getMessage()).contains("접근 권한이 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    @Test
    @DisplayName("매장 조회 - 성공")
    void viewStore(){

        String email = "test@test.com";

        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        Store store = Store.builder()
                .name("테스트 가게")
                .owner(owner)
                .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                .address("서울시")
                .maxCapacity(20)
                .deleted(false)
                .build();

        Store savedStore =  storeDao.saveStore(store);

        StoreResponse getSavedStore = storeService.viewStore(savedStore.getId());

        assertThat(getSavedStore).isNotNull();

        assertThat(getSavedStore.getId()).isEqualTo(savedStore.getId());
        assertThat(getSavedStore.getName()).isEqualTo(store.getName());
        assertThat(getSavedStore.getAddress()).isEqualTo(store.getAddress());

        assertThat(getSavedStore.getLatitude()).isEqualTo(store.getLocation().getY());
        assertThat(getSavedStore.getLongitude()).isEqualTo(store.getLocation().getX());
    }

    @Test
    @DisplayName("매장 조회 - 실패 , 매장없음")
    void viewStore_noStore(){

        String email = "test@test.com";

        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        Store store = Store.builder()
                .name("테스트 가게")
                .owner(owner)
                .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                .address("서울시")
                .maxCapacity(20)
                .deleted(false)
                .build();

        Store savedStore =  storeDao.saveStore(store);

        CustomException exception = assertThrows(CustomException.class, () -> {
            storeService.viewStore(savedStore.getId() + 1);
        });

        assertThat(exception.getMessage()).contains("매장이 존재하지 않습니다.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("매장 리스트(사장님) - 성공")
    void storeList() {

        String email = "test@test.com";

        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        User owner1 = User.builder()
                .email("1"+email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner1);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("password")
                .authorities("ROLE_OWNER")
                .build();


        List<Store> storeList = Arrays.asList(
                Store.builder()
                        .name("테스트 가게1")
                        .owner(owner)
                        .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                        .address("서울시")
                        .maxCapacity(20)
                        .deleted(false)
                        .build(),
                Store.builder()
                        .name("테스트 가게2")
                        .owner(owner1)
                        .location(geometryFactory.createPoint(new Coordinate(128.0, 38.0)))
                        .address("서울시2층")
                        .maxCapacity(25)
                        .deleted(false)
                        .build(),
                Store.builder()
                        .name("테스트 가게3")
                        .owner(owner1)
                        .location(geometryFactory.createPoint(new Coordinate(128.0, 38.0)))
                        .address("서울시2층")
                        .maxCapacity(25)
                        .deleted(true)
                        .build(),
                Store.builder()
                        .name("테스트 가게4")
                        .owner(owner)
                        .location(geometryFactory.createPoint(new Coordinate(128.0, 38.0)))
                        .address("서울시3층")
                        .maxCapacity(25)
                        .deleted(false)
                        .build()
        );

        for(Store store : storeList){
            storeDao.saveStore(store);
        }

        List<StoreResponse> responses = storeService.viewMyStore(userDetails);

        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(storeList.size() - 2);

    }

    //매장리스트 실패 - 권한 실패
    @Test
    @DisplayName("매장 리스트(사장님) - 실패")
    void storeList_fail() {
        String email = "test@test.com";

        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .type(Role.OWNER)
                .build();

        userDao.save(owner);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("1"+email)
                .password("password")
                .authorities("ROLE_OWNER")
                .build();

        CustomException exception = assertThrows(CustomException.class , () -> {
           storeService.viewMyStore(userDetails);
        });

        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    //지도상 매장 표시
    @Test
    @DisplayName("지도상 매장 리스트 표시")
    void storeListTest(){
        List<Store> storeList = Arrays.asList(
                Store.builder()
                        .name("테스트 가게1")
                        .location(geometryFactory.createPoint(new Coordinate(127.0, 37.0)))
                        .address("서울시")
                        .maxCapacity(20)
                        .deleted(false)
                        .build(),
                Store.builder()
                        .name("테스트 가게2")
                        .location(geometryFactory.createPoint(new Coordinate(128.0, 38.0)))
                        .address("서울시2층")
                        .maxCapacity(25)
                        .deleted(false)
                        .build(),
                Store.builder()
                        .name("테스트 가게3")
                        .location(geometryFactory.createPoint(new Coordinate(128.0, 38.0)))
                        .address("서울시2층")
                        .maxCapacity(25)
                        .deleted(false)
                        .build(),
                Store.builder()
                        .name("테스트 가게4")
                        .location(geometryFactory.createPoint(new Coordinate(128.0, 38.0)))
                        .address("서울시3층")
                        .maxCapacity(25)
                        .deleted(false)
                        .build()
        );

        for(Store store : storeList){
            storeDao.saveStore(store);
        }

        LocationRequest request = LocationRequest.builder()
                .longitude(128.0)
                .latitude(38.0)
                .radius(1000.0)
                .build();

        List<StoreResponse> responses = storeService.storeList(request);

        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(storeList.size() - 1);
    }

}
