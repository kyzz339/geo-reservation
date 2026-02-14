package com.jaehyun.demo.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    public SignUpResponse signUp(SignUpRequest request) {

        if(userDao.existsByEmail(request.getEmail())){
            throw new CustomException(ErrorCode.ALREADY_EXIST_USER , "email : " + request.getEmail());
        }

        User savedUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .type(request.getType() != null ? request.getType() : Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userDao.save(savedUser);

        return new SignUpResponse(savedUser.getEmail() , savedUser.getName());
    }

    public TokenResponse signIn(SignInRequest signInRequest) {

        User user = userDao.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND , "email : " + signInRequest.getEmail()));

        if(!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getType());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getType());

        redisTemplate.opsForValue().set(
                "RT:" + signInRequest.getEmail(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenValidity(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse reissue(String refreshToken){

        if(!jwtTokenProvider.validateToken(refreshToken)){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.TOKEN_MISMATCH);
        }

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND , "email : " + email));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getType());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String findByEmail(String email){

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND , "email : " + email));

        return user.getName();
    }
}
