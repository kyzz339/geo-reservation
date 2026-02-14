package com.jaehyun.demo.service.integrationTest;

import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import com.jaehyun.demo.service.StoreService;
import com.jaehyun.demo.service.integrationTest.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @DisplayName("매장 생성 통합 테스트")
    void createStoreIntegrationTest(){

        String email = "test@test.com";
        User owner = User.builder()
                .email(email)
                .name("사장님")
                .password("password")
                .createdAt(java.time.LocalDateTime.now())
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
}
