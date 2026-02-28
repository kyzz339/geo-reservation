package com.jaehyun.demo.service;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.common.exception.ErrorCode;
import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.request.store.LocationRequest;
import com.jaehyun.demo.dto.request.store.UpdateStoreRequest;
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

import java.time.LocalDateTime;
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND , "ID : " + userDetails.getUsername()));

        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

        Store store = Store.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(location)
                .address(request.getAddress())
                .maxCapacity(request.getMaxCapacity())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .owner(owner)
                .build();

        Store savedStore = this.storeDao.saveStore(store);

        return CreateStoreResponse.builder()
                .id(savedStore.getId())
                .name(savedStore.getName())
                .build();
    }

    //매장 수정
    @Transactional
    public StoreResponse updateStore(UpdateStoreRequest request, UserDetails userDetails) {
        Store savedStore = this.storeDao.getStore(request.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND, "StoreId : " + request.getId()));

        if (!savedStore.getOwner().getEmail().equals(userDetails.getUsername())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "ID : " + userDetails.getUsername());
        }

        if (request.getName() != null) savedStore.setName(request.getName());
        if (request.getDescription() != null) savedStore.setDescription(request.getDescription());
        if (request.getAddress() != null) savedStore.setAddress(request.getAddress());
        if (request.getMaxCapacity() != null) savedStore.setMaxCapacity(request.getMaxCapacity());
        if (request.getOpenTime() != null) savedStore.setOpenTime(request.getOpenTime());
        if (request.getCloseTime() != null) savedStore.setCloseTime(request.getCloseTime());
        if (request.getActive() != null) savedStore.setActive(request.getActive());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
            savedStore.setLocation(location);
        }

        return StoreResponse.from(savedStore);
    }
    //매장 삭제
    @Transactional
    public DeleteStoreResponse deleteStore(Long id , UserDetails userdetail) {

        Store savedStore = this.storeDao.getStore(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND , "StoreId : " + id));

        if(!savedStore.getOwner().getEmail().equals(userdetail.getUsername())){
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS , "ID : " + userdetail.getUsername());
        }

        savedStore.setDeleted(true);
        savedStore.setDeletedAt(LocalDateTime.now());

        return DeleteStoreResponse.builder()
                .id(savedStore.getId())
                .name(savedStore.getName())
                .build();
    }
    //매장 조회
    public StoreResponse viewStore(Long id){

        Store savedStore = this.storeDao.getStore(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND , "StoreId : " + id));

        return StoreResponse.from(savedStore);
    }

    //사장본인 매장 조회
    public List<StoreResponse> viewMyStore(UserDetails userDetails){

        User owner = userDao.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND , "ID : " + userDetails.getUsername()));

        return this.storeDao.viewMyStore(owner)
                .stream()
                .map(StoreResponse::from)
                .toList();
    }

    //지도상 매장 리스트(모든 매장 표시) -> 지도 표시
    public List<StoreResponse> storeList(LocationRequest request){

        Point myLocation = geometryFactory.createPoint(new Coordinate(request.getLongitude() , request.getLatitude()));
        Double radius = (request.getRadius() != null) ? request.getRadius() : 1000.0;

        List<Store> stores = storeDao.listStoreNearBy(myLocation , radius);
        return stores.stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());
    }
}
