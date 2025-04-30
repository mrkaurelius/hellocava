package dev.kumru.javaweb.web;

import dev.kumru.javaweb.component.MyComponent;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class MyController {
    private final MyComponent myComponent;

    @GetMapping("/hello")
    String hello() {
        myComponent.doThing();
        return "merhaba yalan dunya";
    }
}

