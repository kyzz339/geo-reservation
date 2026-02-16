package com.jaehyun.demo.core.dao;

import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Repository
public class StoreDao {

    private final StoreRepository storeRepository;

    public Store saveStore(Store store){
        return this.storeRepository.save(store);
    }

    public Optional<Store> getStore(Long id){
        return this.storeRepository.findById(id);
    }

    public List<Store> viewMyStore(User owner){
        return this.storeRepository.findByOwner(owner);
    }

    public List<Store> listStore(){
        return this.storeRepository.findByDeletedIsTrue();
    }

    public List<Store> listStoreNearBy(Point myLocation , Double distance){
        return this.storeRepository.listStoreNearBy(myLocation , distance);
    }

}
