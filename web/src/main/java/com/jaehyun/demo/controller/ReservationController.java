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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<CreateReservationResponse> createReservation(@RequestBody CreateReservationRequest request , @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.reservationService.createReservation(request , userDetails));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/viewStoreReservation")
    public ResponseEntity<List<ReservationResponse>> viewStoreReservation(@ModelAttribute ReservationRequest request , @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.reservationService.viewStoreReservation(request , userDetails));
    }

    @GetMapping("/viewMyReservation")
    public ResponseEntity<List<ReservationResponse>> viewMyReservation(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.reservationService.viewMyReservation(userDetails));
    }

    @PatchMapping("/cancelReservation/{reservationId}")
    public ResponseEntity<UpdateReservationResponse> cancelReservation(@PathVariable(name = "reservationId") Long reservationId , @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(this.reservationService.cancelReservation(reservationId , userDetails));
    }

    @PatchMapping("/changeReservation")
    public ResponseEntity<UpdateReservationResponse> changeReservation(@RequestBody UpdateReservationRequest request , @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(this.reservationService.changeReservation(request, userDetails));
    }

}
