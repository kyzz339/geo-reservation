package com.jaehyun.demo.controller;

import com.jaehyun.demo.dto.request.reservation.CreateReservationRequest;
import com.jaehyun.demo.dto.request.reservation.ReservationRequest;
import com.jaehyun.demo.dto.request.reservation.UpdateReservationRequest;
import com.jaehyun.demo.dto.response.reservation.CreateReservationResponse;
import com.jaehyun.demo.dto.response.reservation.ReservationResponse;
import com.jaehyun.demo.dto.response.reservation.UpdateReservationResponse;
import com.jaehyun.demo.service.ReservationService;
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
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
// --- [View] HTML 페이지 반환 ---

@PreAuthorize("hasRole('USER')")
@GetMapping("/myReservation")
public String myReservationPage() {
    return "reservation/myReservation";
}

// --- [API] JSON 데이터 반환 ---

@ResponseBody
@PreAuthorize("hasRole('USER')")
@PostMapping
public ResponseEntity<CreateReservationResponse> createReservation(@RequestBody CreateReservationRequest request, @AuthenticationPrincipal UserDetails userDetails){
    return ResponseEntity.ok(this.reservationService.createReservation(request, userDetails));
}

// 사장님 매장 예약 확인
@ResponseBody
@PreAuthorize("hasRole('OWNER')")
@GetMapping("/store/{storeId}")
public ResponseEntity<List<ReservationResponse>> viewStoreReservation(@PathVariable Long storeId, @AuthenticationPrincipal UserDetails userDetails){
    ReservationRequest request = ReservationRequest.builder().storeId(storeId).build();
    return ResponseEntity.ok(this.reservationService.viewStoreReservation(request, userDetails));
}

// 손님 본인 예약 확인
@ResponseBody
@PreAuthorize("hasRole('USER')")
@GetMapping("/my")
public ResponseEntity<List<ReservationResponse>> viewMyReservation(@AuthenticationPrincipal UserDetails userDetails){
    return ResponseEntity.ok(this.reservationService.viewMyReservation(userDetails));
}
    @ResponseBody
    @PostMapping("/cancel/{id}")
    public ResponseEntity<UpdateReservationResponse> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.reservationService.cancelReservation(id, userDetails));
    }

    @ResponseBody
    @PostMapping("/update")
    public ResponseEntity<UpdateReservationResponse> changeReservation(@RequestBody UpdateReservationRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.reservationService.changeReservation(request, userDetails));
    }
}
