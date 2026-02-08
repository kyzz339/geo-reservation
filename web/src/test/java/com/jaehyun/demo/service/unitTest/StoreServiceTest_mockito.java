package com.jaehyun.demo.service.unitTest;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.response.store.DeleteStoreResponse;
import com.jaehyun.demo.service.StoreService;
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
import org.springframework.cglib.core.Local;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class StoreServiceTest_mockito {

    @Mock
    private UserDao userDao;
    @Mock
    private StoreDao storeDao;

    /**
     * @Spy : 실체 객체의 메서드를 호출하되, 특정 메시지만 모킹 하거나 감시할 떄 사용,
     * 여기서는 GeometryFactory의 좌표 생성로직을 그대로 활용하기 위해 사용.
     * Precisionmodel: 수치 정밀도 설정(기본값 사용)
     */
    @Spy
    private GeometryFactory geomereyFactory = new GeometryFactory(new PrecisionModel() , 4326);

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("정상 가게 생성 테스트")
    void createStoreTest(){

        String email = "test@test.com";

        User exsistUser = User.builder()
                .id(1L)
                .email(email)
                .name("name")
                .type(Role.OWNER)
                .build();

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);
        when(userDao.findByEmail(email)).thenReturn(Optional.of(exsistUser));

        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);

        when(storeDao.saveStore(any(Store.class))).thenAnswer(inv ->{
            Store s = inv.getArgument(0);
            return Store.builder().id(100L).name(s.getName()).build();
        });

        CreateStoreRequest request = CreateStoreRequest.builder()
                .name("테스트 가게")
                .description("테스트 가게입니다.")
                .longitude(17.00)
                .latitude(18.00)
                .maxCapacity(10)
                .address("서울시 무슨구 무슨동 12-1")
                .build();

        storeService.createStore(request , userDetails);

        verify(storeDao).saveStore(storeCaptor.capture());
        Store captureStore = storeCaptor.getValue();

        assertEquals(exsistUser , captureStore.getOwner());
        assertEquals(request.getName() , captureStore.getName());
        assertEquals(request.getLongitude() , captureStore.getLocation().getX());
        assertEquals(request.getLatitude() , captureStore.getLocation().getY());

        verify(geomereyFactory, times(1)).createPoint(any(Coordinate.class));
    }

    @Test
    @DisplayName("계정 없음")
    void createStoreTest_no_owner(){
        String email = "test@test.com";

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        CreateStoreRequest request = CreateStoreRequest.builder()
                .name("테스트 가게")
                .description("테스트 가게입니다.")
                .longitude(17.00)
                .latitude(18.00)
                .address("서울시 무슨구 무슨동 12-1")
                .maxCapacity(10)
                .build();

        CustomException exception = assertThrows(CustomException.class , () -> {
            storeService.createStore(request , userDetails);
        });

        assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다."));
        verify(storeDao,never()).saveStore(any(Store.class));

    }

    @Test
    @DisplayName("가게 삭제 - 정상")
    void deleteStore_success()throws AccessDeniedException {

        String email = "test@test.com";

        UserDetails userDetail = mock(UserDetails.class);
        when(userDetail.getUsername()).thenReturn(email);

        User existUser = User.builder()
                .id(11L)
                .email(email)
                .build();

        Store existStore = Store.builder()
                .id(10L)
                .name("기존가게")
                .description("삭제 예정")
                .location(geomereyFactory.createPoint(new Coordinate(17.0 , 18.9)))
                .address("삭제 예정 가게 주소")
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .owner(existUser)
                .build();

        when(storeDao.getStore(10L)).thenReturn(Optional.of(existStore));

        DeleteStoreResponse res =  storeService.deleteStore(10L , userDetail);


        assertTrue(existStore.isDeleted());
        assertNotNull(existStore.getDeletedAt());
        assertEquals(res.getId() , existStore.getId());
        assertEquals(res.getName() , existStore.getName());

        verify(storeDao , times(1)).getStore(10L);
    }

    @Test
    @DisplayName("가게 삭제 - 다른계정")
    void deleteStore_otherAccount(){

        String email = "test@test.com";
        String other_email = "other@test.com";
        UserDetails userDetail = mock(UserDetails.class);

        when(userDetail.getUsername()).thenReturn(other_email);
        User existUser = User.builder()
                .id(11L)
                .email(email)
                .build();

        Store existStore = Store.builder()
                .id(10L)
                .name("기존가게")
                .description("삭제 예정")
                .location(geomereyFactory.createPoint(new Coordinate(17.0 , 18.9)))
                .address("삭제 예정 가게 주소")
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .owner(existUser)
                .build();

        when(storeDao.getStore(existStore.getId())).thenReturn(Optional.of(existStore));

        CustomException exception = assertThrows(CustomException.class, () ->{
            storeService.deleteStore(10L , userDetail);
        });

        assertTrue(exception.getMessage().contains("접근 권한이 없습니다."));
        assertFalse(existStore.isDeleted());
        verify(storeDao).getStore(existStore.getId());
    }

    @Test
    @DisplayName("가게 삭제 - 가게 미존재")
    void deleteStore_noStore(){

        Long wrongId = 11L;
        UserDetails userDetail = mock(UserDetails.class);

        when(storeDao.getStore(wrongId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> {
           storeService.deleteStore(wrongId , userDetail);
        });

        assertTrue(exception.getMessage().contains("매장이 존재하지 않습니다."));

        verify(userDetail , never()).getUsername();
    }

}
