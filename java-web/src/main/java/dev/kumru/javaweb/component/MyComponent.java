package dev.kumru.javaweb.component;

import dev.kumru.javaweb.api.MyAPI;
import io.micrometer.core.annotation.Counted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyComponent implements MyAPI {

    @Counted
    @Override
    public void doThing() {
        log.debug(">>> doing something");
    }
}
