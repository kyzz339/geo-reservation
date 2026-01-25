package com.jaehyun.demo.controller;


import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import com.jaehyun.demo.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {

    private StoreService storeService;

    @PostMapping("/createStore")
    @ResponseBody
    public ResponseEntity<CreateStoreResponse> createStore(@RequestBody CreateStoreRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails){

        CreateStoreResponse createStoreResponse = storeService.createStore(request , userDetails);

        return ResponseEntity.status(200).body(createStoreResponse);
    }

}
