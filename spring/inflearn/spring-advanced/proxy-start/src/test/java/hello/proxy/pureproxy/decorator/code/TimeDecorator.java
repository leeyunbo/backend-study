package hello.proxy.pureproxy.decorator.code;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class TimeDecorator extends Decorator {

    public TimeDecorator(Component component) {
        super(component);
    }

    @Override
    public String operation() {
        log.info("TimeDecorator 실행");
        LocalDateTime startTime = LocalDateTime.now();
        String result = component.operation();
        return "[" + startTime + "] " + result;
    }
}
