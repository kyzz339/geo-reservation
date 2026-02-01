package com.jaehyun.demo.service.integrationTest;

import com.jaehyun.demo.core.dao.StoreDao;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StoreServiceTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StoreDao storeDao;



}
