package hello.advanced.app.v7;

import hello.advanced.trace.templatecallback.TraceTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceV7 {

    private final OrderRepositoryV7 orderRepository;
    private final TraceTemplate traceTemplate;

    public void orderItem(String itemId) {
        traceTemplate.execute("OrderController.request()", () -> {
            orderRepository.save(itemId);
            return null;
        });
    }
}
