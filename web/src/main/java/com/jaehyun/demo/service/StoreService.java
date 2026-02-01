package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import com.jaehyun.demo.dto.response.store.DeleteStoreResponse;
import com.jaehyun.demo.dto.response.store.StoreResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserDao userDao;
    private final StoreDao storeDao;

    private final GeometryFactory geometryFactory;

    //매장 생성 -> owner
    @Transactional
    public CreateStoreResponse createStore(CreateStoreRequest request , UserDetails userDetails){

        User owner = userDao.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

        Store store = Store.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(location)
                .address(request.getAddress())
                .active(Boolean.TRUE)
                .createdAt(OffsetDateTime.now())
                .deleted(Boolean.FALSE)
                .owner(owner)
                .build();

        Store savedStore = this.storeDao.saveStore(store);

        return CreateStoreResponse.builder()
                .id(savedStore.getId())
                .name(savedStore.getName())
                .build();
    }
    //매장 삭제
    @Transactional
    public DeleteStoreResponse deleteStore(Long id , UserDetails userdetail) throws AccessDeniedException {

        Store savedStore = this.storeDao.getStore(id)
                .orElseThrow(() -> new IllegalArgumentException("deleteStore : 가게가 존재하지 않습니다. id =" + id));

        if(!savedStore.getOwner().getEmail().equals(userdetail.getUsername())){
            throw new AccessDeniedException("deleteStore : 삭제 권한이 없습니다. id :" + id);
        }

        savedStore.setDeleted(true);
        savedStore.setDeletedAt(OffsetDateTime.now());

        return DeleteStoreResponse.builder()
                .id(savedStore.getId())
                .name(savedStore.getName())
                .build();
    }
    //매장 조회
    public StoreResponse viewStore(Long id){

        Store savedStore = this.storeDao.getStore(id)
                .orElseThrow(() -> new IllegalArgumentException("viewStore :  가게가 존재하지 않습니다. id = "+ id));

        return StoreResponse.from(savedStore);
    }

    //매장 리스트(모든 매장 표시) -> 지도 표시
    public List<StoreResponse> storeList(){

        List<Store> stores = storeDao.listStore();

        return stores.stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());
    }
}
