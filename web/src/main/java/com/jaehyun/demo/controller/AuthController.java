package com.jaehyun.demo.controller;

import com.jaehyun.demo.common.exception.CustomException;
import com.jaehyun.demo.common.exception.ErrorCode;
import com.jaehyun.demo.dto.request.auth.ReissueRequest;
import com.jaehyun.demo.dto.request.auth.SignInRequest;
import com.jaehyun.demo.dto.request.auth.SignUpRequest;
import com.jaehyun.demo.dto.response.auth.SignUpResponse;
import com.jaehyun.demo.dto.response.auth.TokenResponse;
import com.jaehyun.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/signUp")
    public String signUp() {
        return "auth/signUp";
    }

    @GetMapping("/signIn")
    public String signIn() {
        return "auth/signIn";
    }

    @ResponseBody
    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        SignUpResponse signUpResponse = authService.signUp(signUpRequest);
        return ResponseEntity.status(201).body(signUpResponse);
    }

    @ResponseBody
    @PostMapping("/signIn")
    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRequest request){
        TokenResponse tokenResponse = authService.signIn(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @ResponseBody
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody ReissueRequest request){
        return ResponseEntity.ok(authService.reissue(request.getRefreshToken()));
    }

    @ResponseBody
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(@AuthenticationPrincipal UserDetails userDetails){
        if (userDetails == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "인증 정보가 없습니다.");
        }

        String email = userDetails.getUsername();
        String name = authService.findByEmail(email);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("roles", roles);

        return ResponseEntity.ok(response);
    }

}
