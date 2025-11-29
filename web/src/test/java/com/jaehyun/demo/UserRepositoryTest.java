package com.jaehyun.demo;

import com.jaehyun.demo.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(classes = WebApplication.class)
public class UserRepositoryTest {

    private final UserRepository userRepository;

    @Test
    void findByEmailTest(){

    }

}
