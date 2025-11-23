package com.jaehyun.demo.core.repository;

import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@EntityScan(basePackages = "com.jaehyun.demo.core.entity")
@EnableJpaRepositories(basePackages = "com.jaehyun.demo.core.repository")
@ComponentScan(basePackages = "com.jaehyun.demo.core")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind(){

        User user = new User(null, "jaehyun@jaehyun.com" , "password" , "jaehyun" , Role.USER);
        userRepository.save(user);

        User findUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(findUser.getName()).isEqualTo("jaehyun");
    }


}
