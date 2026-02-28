package com.jaehyun.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"))) {
            return "redirect:/myStore";
        }
        return "index";
    }

    @GetMapping("/myStore")
    public String viewMyStore() {
        return "store/viewMyStore";
    }

    @GetMapping("/createStore")
    public String createStore() {
        return "store/createStore";
    }

    @GetMapping("/store/manage/{id}")
    public String manageStore() {
        return "store/manageStore";
    }

}
