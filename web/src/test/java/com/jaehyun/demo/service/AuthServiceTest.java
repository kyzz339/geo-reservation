package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.SignInRequest;
import com.jaehyun.demo.dto.response.TokenResponse;
import com.jaehyun.demo.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 테스트")
    void signUp_Success(){
        User user = User.builder()          //user 생성
                .email("test@test.com")
                .password("raw-password")
                .name("테스트 계정")
                .build();

        //passwordEncoder.encode("raw-password")) 는 무조건 encoded-password return -> mock은 return 을 안함
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");

        authService.signUp(user);

        verify(passwordEncoder, times(1)).encode("raw-password");
        verify(userDao, times(1)).save(argThat(savedUser ->
                savedUser.getEmail().equals("test@test.com")
                && savedUser.getPassword().equals("encoded-password")
                && savedUser.getName().equals("테스트 계정")
        ));
    }

    @Test
    @DisplayName("로그인 테스트")
    void signIn_Success(){
        SignInRequest signInRequest = SignInRequest.builder()
                .email("test@test.com")
                .password("password")
                .build();

        User exsistUser = User.builder()
                        .id(1L)
                        .email("test@test.com")
                        .name("name")
                        .type(Role.USER)
                        .password("encoded-password")
                        .build();

        when(userDao.findByEmail("test@test.com")).thenReturn(Optional.of(exsistUser));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(),any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString(),any())).thenReturn("refreshToken");

        TokenResponse response =  authService.signIn(signInRequest);

        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

        verify(userDao, times(1)).findByEmail("test@test.com");

    }


}
