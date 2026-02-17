package com.jaehyun.demo.controller;


import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.request.store.LocationRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import com.jaehyun.demo.dto.response.store.DeleteStoreResponse;
import com.jaehyun.demo.dto.response.store.StoreResponse;
import com.jaehyun.demo.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/createStore")
    public ResponseEntity<CreateStoreResponse> createStore(@RequestBody CreateStoreRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.createStore(request , userDetails));
    }

    @PostMapping("/deleteStore/{id}")
    public ResponseEntity<DeleteStoreResponse> deleteStore(@PathVariable(name = "id") Long id , @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.deleteStore(id , userDetails));
    }

    @GetMapping("/viewStore/{id}")
    public ResponseEntity<StoreResponse> viewStore(@PathVariable(name = "id") Long id){
        return ResponseEntity.ok(this.storeService.viewStore(id));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/viewMyStore")
    public ResponseEntity<List<StoreResponse>> viewMyStore(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.viewMyStore(userDetails));
    }

    @GetMapping("/storeList")
    public ResponseEntity<List<StoreResponse>> storeList(LocationRequest reqeuest){
        return ResponseEntity.ok(this.storeService.storeList(reqeuest));
    }



}
