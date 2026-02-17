package com.jaehyun.demo.config;

import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.core.repository.StoreRepository;
import com.jaehyun.demo.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeometryFactory geometryFactory;

    @Override
    @Transactional
    public void run(String... args) {
        //if (userRepository.count() > 5) return;

        String commonPassword = passwordEncoder.encode("password123!");

        List<User> owners = userRepository.findAll().stream()
                .filter(u -> u.getType() == Role.OWNER)
                .toList();

        if (owners.size() < 10) {
            owners = new ArrayList<>();
            for (int i = 500; i <= 600; i++) {
                User owner = User.builder()
                        .email("owner" + i + "@test.com")
                        .password(commonPassword)
                        .name("대량사장님" + i)
                        .type(Role.OWNER)
                        .build();
                owners.add(userRepository.save(owner));
            }
        }

        // 2. 지역 설정 데이터 클래스 (간단하게 내부 클래스나 Map으로 활용 가능)
        // 지역명, 위도, 경도, 생성할 매장 개수
        createStoresInRegion(owners, "성수", 37.5445, 127.0560, 200);
        createStoresInRegion(owners, "강남", 37.4980, 127.0276, 200);
        createStoresInRegion(owners, "노원", 37.6542, 127.0568, 200);
        createStoresInRegion(owners, "종각", 37.5701, 126.9829, 300);
        createStoresInRegion(owners, "잠실", 37.5133, 127.1001, 100); // 기존 잠실 데이터*/

        createStoresInRegion(owners, "홍대", 37.5565, 126.9239, 300);
        createStoresInRegion(owners, "이태원", 37.5345, 126.9946, 250);
        createStoresInRegion(owners, "압구정", 37.5270, 127.0331, 250);
        createStoresInRegion(owners, "성수", 37.5445, 127.0560, 300); // 누적 생성
        createStoresInRegion(owners, "강남", 37.4980, 127.0276, 300); // 누적 생성
        createStoresInRegion(owners, "여의도", 37.5216, 126.9242, 200);
        createStoresInRegion(owners, "대학로", 37.5822, 127.0019, 200);
        createStoresInRegion(owners, "혜화", 37.5822, 127.0019, 200);
        createStoresInRegion(owners, "광화문", 37.5716, 126.9765, 250);
        createStoresInRegion(owners, "판교", 37.3947, 127.1111, 300);

        System.out.println(">>> 총 1,000개의 매장 데이터 생성이 완료되었습니다.");
    }

    private void createStoresInRegion(List<User> owners, String regionName, double baseLat, double baseLng, int count) {
        List<Store> stores = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            // 지역 중심 좌표에서 약 1~2km 반경 내 랜덤 분포 (0.02도 정도)
            double lat = baseLat + (Math.random() - 0.5) * 0.02;
            double lng = baseLng + (Math.random() - 0.5) * 0.02;

            Point location = geometryFactory.createPoint(new Coordinate(lng, lat));
            location.setSRID(4326);

            // 생성된 사장님들 중 한 명에게 랜덤하게 할당
            User randomOwner = owners.get((int) (Math.random() * owners.size()));

            Store store = Store.builder()
                    .name(regionName + " " + i + "호점")
                    .description(regionName + " 지역의 맛집 " + i + "번 매장입니다.")
                    .location(location)
                    .address("서울시 " + regionName + " 인근 도로 " + i)
                    .maxCapacity(10 + (int) (Math.random() * 30))
                    .owner(randomOwner)
                    .active(true)
                    .build();

            stores.add(store);

            // Batch Insert 성능을 위해 100개 단위로 저장 (선택 사항)
            if (i % 100 == 0) {
                storeRepository.saveAll(stores);
                stores.clear();
            }
        }
        // 남은 데이터 저장
        if (!stores.isEmpty()) {
            storeRepository.saveAll(stores);
        }
    }
}
