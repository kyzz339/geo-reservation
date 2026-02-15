package com.jaehyun.demo.service.integrationTest;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.common.exception.ErrorCode;
import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.auth.SignInRequest;
import com.jaehyun.demo.dto.request.auth.SignUpRequest;
import com.jaehyun.demo.dto.response.auth.SignUpResponse;
import com.jaehyun.demo.dto.response.auth.TokenResponse;
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
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("회원 가입 실패-계정 중복")
    void signUpFail_duplication(){
        String email = "test@test.com";
        String name = "name";
        String password = "password";

        User duplicateUser = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .type(Role.USER)
                .build();

        userDao.save(duplicateUser);

        SignUpRequest request = SignUpRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .type(Role.USER)
                .build();

        CustomException exception = assertThrows(CustomException.class, () ->{
            authService.signUp(request);
        });

        assertTrue(exception.getMessage().contains("이미 가입된 이메일입니다."));
        assertEquals(exception.getErrorCode() , ErrorCode.ALREADY_EXIST_USER);

    }

    @Test
    @DisplayName("로그인 - 성공")
    void SignIn_success(){
        String email = "test@test.com";
        String name = "name";
        String password = "testpassword";

        User loginAccount = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .type(Role.USER)
                .build();

        userDao.save(loginAccount);

        SignInRequest request = SignInRequest.builder()
                .email(email)
                .password(password)
                .build();

        TokenResponse tokenResponse = authService.signIn(request);

        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).isNotNull();

        String redisKey = "RT:" + email;
        String savedRefreshToken = (String) redisTemplate.opsForValue().get(redisKey);

        assertThat(savedRefreshToken).isNotNull();
        assertThat(savedRefreshToken).isEqualTo(tokenResponse.getRefreshToken());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void signIn_fail_unmatchedPassword(){
        String email = "test@test.com";
        String name = "name";
        String password = "testpassword";

        User existAccount = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .type(Role.USER)
                .build();

        userDao.save(existAccount);

        SignInRequest request = SignInRequest.builder()
                .email(email)
                .password(password + "1")
                .build();

        CustomException exception = assertThrows(CustomException.class, () ->{
            authService.signIn(request);
        });

        assertThat(exception.getMessage()).contains("비밀번호가 일치하지 않습니다.");
        assertEquals(exception.getErrorCode() , ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 미존재")
    void signIn_fail_NoAccount(){
        String email = "test@test.com";
        String name = "name";
        String password = "testpassword";

        User loginAccount = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .type(Role.USER)
                .build();

        userDao.save(loginAccount);

        SignInRequest request = SignInRequest.builder()
                .email("1" + email)
                .password(password)
                .build();

        CustomException exception = assertThrows(CustomException.class,() ->{
            authService.signIn(request);
        });

        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);

    }

    @Test
    @DisplayName("accessToken 재발행")
    void reissueAccessToken(){

        String email = "test@test.com";
        String name = "name";
        String password = "testpassword";

        User loginAccount = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .type(Role.USER)
                .build();

        userDao.save(loginAccount);

        String refreshToken = jwtTokenProvider.generateRefreshToken(loginAccount.getEmail() , loginAccount.getType());
        redisTemplate.opsForValue().set("RT:" + email , refreshToken);

        TokenResponse response = authService.reissue(refreshToken);

        assertThat(response.getAccessToken()).isNotNull();

        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        String emailFromToken = jwtTokenProvider.getEmailFromToken(response.getAccessToken());
        assertThat(emailFromToken).isEqualTo(email);

    }
}
