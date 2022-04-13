package hello.advanced.app.v7;

import hello.advanced.trace.templatecallback.TraceTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderControllerV7 {

    private final OrderServiceV7 orderService;
    private final TraceTemplate traceTemplate;

    @GetMapping("/v7/request")
    public String request(String itemId) {
        return traceTemplate.execute("OrderController.request()", () -> {
            orderService.orderItem(itemId);
            return "ok";
        });
    }
}
