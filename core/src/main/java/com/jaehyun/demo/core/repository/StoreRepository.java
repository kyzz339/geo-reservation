package com.jaehyun.demo.core.repository;

import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByDeletedIsTrue();

    List<Store> findByOwner(User user);

}
