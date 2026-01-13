package com.jaehyun.demo.core.dao;

import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Log4j2
@RequiredArgsConstructor
@Repository
public class StoreDao {

    private final StoreRepository storeRepository;

    public Store saveStore(Store store){
        return this.storeRepository.save(store);
    }

}
