package com.jaehyun.demo.service.integrationTest;

import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.auth.SignUpRequest;
import com.jaehyun.demo.dto.response.auth.SignUpResponse;
import com.jaehyun.demo.jwt.JwtTokenProvider;
import com.jaehyun.demo.service.AuthService;
import com.jaehyun.demo.service.integrationTest.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest_integrationTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String , String> redisTemplate;


    @Test
    @DisplayName("회원가입(회원) - 성공")
    void signUpTest(){

        String email = "test@test.com";
        String name = "name";
        String password = "password";

        SignUpRequest request = SignUpRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .type(Role.USER)
                .build();

        SignUpResponse response = authService.signUp(request);

        assertThat(response.getEmail()).isNotNull();
        assertThat(response.getName()).isEqualTo(name);

        User savedUser = userDao.findByEmail(email).orElseThrow();

        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getName()).isEqualTo(name);
        assertThat(savedUser.getPassword()).isNotEqualTo(request.getPassword());
        assertThat(passwordEncoder.matches(password , savedUser.getPassword())).isTrue();

    }
}
