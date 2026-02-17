package com.jaehyun.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(){
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

}
