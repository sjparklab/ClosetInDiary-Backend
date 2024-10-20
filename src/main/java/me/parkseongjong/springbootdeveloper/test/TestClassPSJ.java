package me.parkseongjong.springbootdeveloper.test;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestClassPSJ {

    @GetMapping("/test-psj")
    public String testpsjget() {
        return "Hello-GET";
    }

    @PostMapping("/test-psj")
    public String testpsjpost() {
        return "Hello-POST";
    }
}
