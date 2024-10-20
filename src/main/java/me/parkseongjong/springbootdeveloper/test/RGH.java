package me.parkseongjong.springbootdeveloper.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RGH {

    @GetMapping("/test-rgh")
    public String testbchget(){
        return "hello get";
    }
    @PostMapping("/test-rgh")
    public String testbchpost(){
        return "hello post";
    }
}

