package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.SignInRequest;
import com.jaehyun.demo.dto.response.TokenResponse;
import com.jaehyun.demo.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public void signUp(User user) {

        User savedUser = User.builder()
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .name(user.getName())
                .type(user.getType() != null ? user.getType() : Role.USER)
                .build();

        userDao.save(savedUser);
    }

    public TokenResponse signIn(SignInRequest signInRequest) {

        User user = userDao.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 입니다."));

        if(!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getType());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getType());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


}
