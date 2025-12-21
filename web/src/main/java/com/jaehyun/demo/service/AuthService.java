package com.jaehyun.demo.service;

import com.jaehyun.demo.core.dao.UserDao;
import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.enums.Role;
import com.jaehyun.demo.dto.request.SignInRequest;
import com.jaehyun.demo.dto.request.SignUpRequest;
import com.jaehyun.demo.dto.response.SignUpResponse;
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

    public SignUpResponse signUp(SignUpRequest request) {

        if(userDao.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
        }

        User savedUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .type(request.getType() != null ? request.getType() : Role.USER)
                .build();

        userDao.save(savedUser);

        return new SignUpResponse(savedUser.getEmail() , savedUser.getName());
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

    public String findByEmail(String email){

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 입니다."));

        return user.getName();
    }
}
