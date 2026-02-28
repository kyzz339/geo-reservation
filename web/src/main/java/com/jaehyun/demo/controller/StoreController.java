package com.jaehyun.demo.controller;

import com.jaehyun.demo.dto.request.store.CreateStoreRequest;
import com.jaehyun.demo.dto.request.store.LocationRequest;
import com.jaehyun.demo.dto.request.store.UpdateStoreRequest;
import com.jaehyun.demo.dto.response.store.CreateStoreResponse;
import com.jaehyun.demo.dto.response.store.DeleteStoreResponse;
import com.jaehyun.demo.dto.response.store.StoreResponse;
import com.jaehyun.demo.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/myStore")
    public String viewMyStorePage() {
        return "store/viewMyStore";
    }

    @GetMapping("/createStore")
    public String createStorePage() {
        return "store/createStore";
    }

    @GetMapping("/manage/{id}")
    public String manageStorePage() {
        return "store/manageStore";
    }

    @GetMapping("/reservation/{id}")
    public String viewStoreReservationPage() {
        return "store/storeReservation";
    }

    // --- [API] JSON 데이터 반환 (실제 보안 적용) ---

    @ResponseBody
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/createStore")
    public ResponseEntity<CreateStoreResponse> createStore(@RequestBody CreateStoreRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.createStore(request, userDetails));
    }

    @ResponseBody
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/updateStore")
    public ResponseEntity<StoreResponse> updateStore(@RequestBody UpdateStoreRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.updateStore(request, userDetails));
    }

    @ResponseBody
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/deleteStore/{id}")
    public ResponseEntity<DeleteStoreResponse> deleteStore(@PathVariable(name = "id") Long id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.deleteStore(id, userDetails));
    }

    @ResponseBody
    @GetMapping("/viewStore/{id}")
    public ResponseEntity<StoreResponse> viewStore(@PathVariable(name = "id") Long id){
        return ResponseEntity.ok(this.storeService.viewStore(id));
    }

    @ResponseBody
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/viewMyStore")
    public ResponseEntity<List<StoreResponse>> viewMyStore(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.storeService.viewMyStore(userDetails));
    }

    @ResponseBody
    @GetMapping("/storeList")
    public ResponseEntity<List<StoreResponse>> storeList(LocationRequest request){
        return ResponseEntity.ok(this.storeService.storeList(request));
    }
}
