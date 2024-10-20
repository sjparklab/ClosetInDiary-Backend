package me.parkseongjong.springbootdeveloper.test;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestClassBCH {

    @GetMapping("/test-bch")
    public String testbchget(){
        return "hello get";
    }
    @PostMapping("/test-bch")
    public String testbchpost(){
        return "hello post";
    }
}

