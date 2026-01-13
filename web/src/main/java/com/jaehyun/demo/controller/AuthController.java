package com.jaehyun.demo.controller;

import com.jaehyun.demo.dto.request.auth.ReissueRequest;
import com.jaehyun.demo.dto.request.auth.SignInRequest;
import com.jaehyun.demo.dto.request.auth.SignUpRequest;
import com.jaehyun.demo.dto.response.auth.SignUpResponse;
import com.jaehyun.demo.dto.response.auth.TokenResponse;
import com.jaehyun.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/signUp")
    public String signUp() {
        return "/auth/signUp";
    }

    @PostMapping("/signUp")
    @ResponseBody
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest signUpRequest) {

        SignUpResponse signUpResponse = authService.signUp(signUpRequest);

        return ResponseEntity.status(201).body(signUpResponse);
    }

    @GetMapping("/signIn")
    public String signIn() {
        return "/auth/signIn";
    }

    @PostMapping("/signIn")
    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRequest request){

        TokenResponse tokenResponse = authService.signIn(request);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody ReissueRequest request){
        return ResponseEntity.ok(authService.reissue(request.getRefreshToken()));
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getMyInfo(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        String name = authService.findByEmail(email);

        Map<String, String> response = new HashMap<>();
        response.put("name", name);

        return ResponseEntity.ok(response);
    }

}
