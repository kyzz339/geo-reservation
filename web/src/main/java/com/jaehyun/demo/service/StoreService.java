package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserDao userDao;
    private final StoreDao storeDao;

    //매장 생성 -> owner
    public CreateStoreResponse createStore(CreateStoreRequest request , UserDetails userDetails){

        User owner = userDao.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Store store = Store.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .address(request.getAddress())
                .active(Boolean.TRUE)
                .owner(owner)
                .build();

        Store savedStore = this.storeDao.saveStore(store);

        return CreateStoreResponse.builder()
                .id(savedStore.getId())
                .name(savedStore.getName())
                .build();
    }
    //매장 삭제


    //매장 조회

    //매장 리스트(모든 매장 표시) -> 지도 표시

}
