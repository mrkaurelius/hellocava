package dev.kumru.javaweb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/hello")
    String hello() {
        return "merhaba yalan dunya";
    }
}

