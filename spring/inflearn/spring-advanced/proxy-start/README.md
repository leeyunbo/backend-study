# 프록시, 데코레이터 패턴

## 로그추적기 요구사항 추가
```text
이전의 템플릿 메서드 패턴과, 콜백 페턴을 적용한 로그 추적기는 결국 기존 코드를 수정해야 하는 문제가 존재한다. 
(만약 클래스가 수백개라면, 수백개를 다 고쳐야하는 문제..)
```
- **원본 코드를 전혀 수정하지 않고 로그 추적기를 적용해라.**
- 특정 메서드는 로그를 출력하지 않는 기능 
- 다음과 같은 다양한 케이스에 적용할 수 있어야 한다. 
  - v1 패키지 : 인터페이스가 있는 구현 클래스 
  - v2 패키지 : 인터페이스가 없는 구체 클래스 
  - v3 패키지 : 컴포넌트 스캔 대상에 기능 적용

## 프록시란?
```text
client ---> proxy --> server
```
클라이언트와 서버가 있을 때 클라이언트가 서버에 직접 요청하는 것이 아니라 어떤 대리자를 통해서 간접적으로 서버에 요청할 수 있다. **여기서 대리자를 영어로 Proxy라고 한다.**

#### 대리자는 중간에서 여러 가지 일을 할 수 있다.
- 접근 제어, 캐싱
  - 권한에 따른 접근 차단
  - 캐싱 
  - 지연 로딩  
- 부가 기능 추가
  - 요청, 응답 값 변형
  - 실행시간 측정하여 추가 로깅 
- 프록시 체인 
  - 대리자가 또 다른 대리자를 부를 수 있다, 동생에게 라면을 사달라고 시켰더니 동생은 또 다시 다른 누군가에게 라면을 사오라고 시킬 수 있다. 중요한 점은 클라이언트는 대리자를 통해서 요청했기 때문에 그 이후의 과정은 모른다는 점이다. 이를 프록시 체인이라고 한다. 

#### 대체 가능 
- 객체에서 프록시가 되려면 클라이언트는 서버에게 요청을 한 것인지, 프록시에게 요청을 한 것인지 조차 몰라야 한다.
- 쉽게 이야기해서 서버와 프록시는 같은 인터페이스를 사용해야 하며, 클라이언트가 사용하는 서버 객체를 프록시 객체로 변경해도 클라이언트 코드를 변경하지 않고 동작할 수 있어야 한다. 

#### 프록시 패턴 vs 데코레이터 패턴 
- 둘다 프록시를 사용하는 방법이지만 GoF 디자인 패턴에서는 이 둘을 의도에 따라서 프록시 패턴과 데코레이터 패턴으로 구분한다.
- 프록시 패턴 : 접근 제어가 목적 
- 데코레이터 패턴 : 새로운 기능 추가가 목적

> ##### 참고 : 프록시라는 개념은 어디서든 사용된다. 규모의 차이가 있을 뿐 근본적인 역할은 같다.

#### 프록시 패턴 캐싱 예제
<img width="859" alt="image" src="https://user-images.githubusercontent.com/44944031/163998232-b81bd621-fac1-413a-9c8c-0b75d8149999.png">

- `Proxy`와 `Server`가 `ServerInterface`를 구현하고 있기 때문에 언제든지 DI를 통해 교체가 가능하다.


```java
public class RealSubject implements Subject {

    @Override
    public String operation() {
        log.info("실제 객체 호출");
        sleep(1000);
        return "data";
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
```java
public class ProxyPatternClient {

    private Subject subject;

    public ProxyPatternClient(Subject subject) {
        this.subject = subject;
    }

    public void execute() {
        subject.operation();
    }
}
```
```java
@Test
void noProxyTest() {
    RealSubject realSubject = new RealSubject();
    ProxyPatternClient client = new ProxyPatternClient(realSubject);
    client.execute();
    client.execute();
    client.execute();
}
```
- `RealSubject`의 `operation`은 무려 1초나 걸리는 비즈니스 로직인데, `ProxyPatternClient`에서 이를 3번 호출하면 3초나 소요된다.
- 어차피 동일한 결과를 응답하니까 캐시를 적용하면 1초 정도에 3번의 호출을 처리할 수 있을 것이다.
- 이때 활용할 수 있는 것이 바로 `프록시 패턴`이다.

```java
public class CacheProxy implements Subject {

    private Subject target;
    private String cacheValue;

    public CacheProxy(Subject target) {
        this.target = target;
    }

    @Override
    public String operation() {
        log.info("프록시 호출");
        if (cacheValue == null) {
            cacheValue = target.operation();
        }
        return cacheValue;
    }
}
```
```java
@Test
void cacheProxyTest() {
    RealSubject realSubject = new RealSubject();
    CacheProxy cacheProxy = new CacheProxy(realSubject);

    ProxyPatternClient client = new ProxyPatternClient(cacheProxy);
    client.execute();
    client.execute();
    client.execute();
}
```
- 클라이언트가 `realSubject`가 아닌 `Subject`의 구현체인 `cacheProxy` 프록시 객체를 주입받는다.
- `cacheProxy`는 클라이언트의 요청을 가로챈 후 캐싱하여 데이터를 반환한다.
- 결국 클라이언트의 변화는 하나도 없이 프록시 객체를 주입하여 기능을 추가할 수 있었다.


#### 데코레이터 패턴
- 프록시로 부가 기능을 추가하는 것을 데코레이터 패턴이라고 한다.

<img width="863" alt="image" src="https://user-images.githubusercontent.com/44944031/164002327-63ce1e04-ba7b-4a21-a57e-a48e737eff8c.png">

```java
public interface Component {
    String operation();
}
```
```java
@Slf4j
public class RealComponent implements Component {
    @Override
    public String operation() {
        log.info("RealComponent 실행");
        return "data";
    }
}
```
```java
@Slf4j
public class TimeDecorator implements Component {

    private Component component;

    public TimeDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("TimeDecorator 실행");
        LocalDateTime startTime = LocalDateTime.now();
        String result = component.operation();
        return "[" + startTime + "] " + result;
    }
}
```

```java
@Slf4j
public class MessageDecorator implements Component {

    private Component component;

    public MessageDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("MessageDecorator 실행");

        String result = component.operation();
        String decoResult = "*****" + result + "*****";

        return decoResult;
    }
}
```
- `Component`를 상속받는 `Decorator`들과 실제 비즈니스 로직을 담고 있는 `RealComponent`이다.

```java
@Slf4j
public class DecoratorPatternClient {

    private Component component;

    public DecoratorPatternClient(Component component) {
        this.component = component;
    }

    public void execute() {
        String result = component.operation();
        log.info("result={}", result);
    }
}
```
- `Decorator`들과 `RealComponent`는 `Component` 인터페이스를 구현하는 구현 클래스이기 때문에 클라이언트는 어떠한 `Decorator`들을 모두 주입받을 수 있다. 

```java
@Test
void decorator2() {
    Component realComponent = new RealComponent();
    Component messageDecorator = new MessageDecorator(realComponent);
    Component timeDecorator = new TimeDecorator(messageDecorator);
    DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator);
    client.execute();
}
```
- 다음과 같이 `Decorator`들을 `RealComponent`와 체이닝처럼 연결하여 부가 기능을 클라이언트 코드의 변경 없이 추가할 수 있다.

#### 프록시 패턴, 데코레이터 패턴의 문제점
<img width="852" alt="image" src="https://user-images.githubusercontent.com/44944031/164005790-5e5381d1-673c-478a-927a-3798ffe86238.png">
- 위의 프록시 패턴과 데코레이터 패턴을 살펴보면 문제점이 존재한다. `Decorator`들은 스스로 존재할 수 없다. 항상 꾸며줄 대상이 있어야 한다. 따라서 내부에 호출 대상인 `Component`가 존재해야 한다. 그리고 `Component`를 통해 항상 메서드를 호출해줘야 한다.
- 이런 중복을 제거하기 위해 위의 이미지와 같이 `Component`를 속성으로 가지고 있는 `Decorator`라는 추상 클래스를 만드는 방법도 고민할 수 있다. 
    - 이렇게 하면 추가로 얻을 수 있는 이점은 클래스 다이어그램에서 어떠한 것이 실제 컴포넌트 인지, 데코레이터인지 명확하게 구분할 수 있다. 

#### 결론 
- 프록시 패턴과 데코레이터 패턴은 모양이 정말 비슷하다. 
- 다른 점은 결국 `의도(intent)` 이다. 디자인 패턴은 모양보단 의도가 정말로 중요하다. 
- 프록시 패턴과 데코레이터 패턴은 `의도`에 의해 구분된다.
> **프록시 패턴의 의도** : 다른 개체에 대한 `접근을 제어`하기 위해 데리자를 제공
> **데코레이터 패턴의 의도** : 객체에 `추가 책임을 동적으로 추가`하고, 기능 확장을 위한 유연한 대안 제공


## 프록시 패턴과 데코레이터 패턴을 활용한 로그 추적기 구현 

#### V1 프로젝트 기반 (v1 패키지 : 인터페이스가 있는 구현 클래스)
<img width="866" alt="image" src="https://user-images.githubusercontent.com/44944031/164013412-15109eb3-ff39-4d76-a5ba-bb96be88d384.png">

```java
@RequiredArgsConstructor
public class OrderRepositoryInterfaceProxy implements OrderRepositoryV1 {

    private final OrderRepositoryV1 target;
    private final LogTrace logTrace;

    @Override
    public void save(String itemId) {
        TraceStatus status = null;

        try {
            status = logTrace.begin("OrderRepository.request()");
            // target 호출
            target.save(itemId);
            logTrace.end(status);
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
```
- `Repository`를 위한 `Proxy` 클래스, `OrderRepositoryV1` 인터페이스를 구현했기 때문에 `OrderRepositoryImpl` 대신 주입할 수 있다.
- `Proxy` 클래스는 실제 `Impl` 클래스인 `target`의 메서드를 호출한다. 
  
```java
@RequiredArgsConstructor
public class OrderServiceInterfaceProxy implements OrderServiceV1 {

    private final OrderServiceV1 target;
    private final LogTrace logTrace;

    @Override
    public void orderItem(String itemId) {
        TraceStatus status = null;

        try {
            status = logTrace.begin("OrderService.orderItem()");
            // target 호출
            target.orderItem(itemId);
            logTrace.end(status);
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
```
- `Service`를 위한 `Proxy` 클래스, `OrderServiceV1` 인터페이스를 구현했기 때문에 `OrderServiceImpl` 대신 주입할 수 있다.
  
```java
@RequiredArgsConstructor
public class OrderControllerInterfaceProxy implements OrderControllerV1 {

    private final OrderControllerV1 target;
    private final LogTrace logTrace;

    @Override
    public String request(String itemId) {
        TraceStatus status = null;

        try {
            status = logTrace.begin("OrderController.request()");
            // target 호출
            String result = target.request(itemId);
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }

    @Override
    public String noLog() {
        return target.noLog();
    }
}
```
- `Controller`를 위한 `Proxy` 클래스, `OrderControllerV1` 인터페이스를 구현했기 때문에 `OrderControllerImpl` 대신 주입할 수 있다.

```java
@Configuration
public class InterfaceProxyConfig {

    @Bean
    public OrderControllerInterfaceProxy orderController(LogTrace logTrace) {
        OrderControllerV1Impl controllerImpl = new OrderControllerV1Impl(orderService(logTrace));
        return new OrderControllerInterfaceProxy(controllerImpl, logTrace);
    }

    @Bean
    public OrderServiceInterfaceProxy orderService(LogTrace logTrace) {
        OrderServiceV1Impl serviceImpl = new OrderServiceV1Impl(orderRepository(logTrace));
        return new OrderServiceInterfaceProxy(serviceImpl, logTrace);
    }

    @Bean
    public OrderRepositoryInterfaceProxy orderRepository(LogTrace logTrace) {
        OrderRepositoryV1Impl repositoryImpl = new OrderRepositoryV1Impl();
        return new OrderRepositoryInterfaceProxy(repositoryImpl, logTrace);
    }
}
```
```java
@Import(InterfaceProxyConfig.class)
@SpringBootApplication(scanBasePackages = "hello.proxy.app")
public class ProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @Bean
    public LogTrace logTrace() {
        return new ThreadLocalLogTrace();
    }
}
```
- 그림과 같이 의존관계 설정을 해줘야한다. 
- 기존에는 `Impl` 클래스를 주입해줬었지만, 이젠 Impl 클래스가 아닌 `Proxy` 클래스들을 주입해줘야 한다.
- 정리하면 다음과 같은 의존 관계를 가지고 있다.
    - `proxy` -> `target`
    - `orderServiceInterfaceProxy` -> `orderServiceV1Impl`
- 모두 자바 Heap 메모리로는 올라가지만, `Impl`은 스프링 빈이 관리하진 않는다.

#### 요구사항 만족 확인 
- 원본 코드를 수정하지 않아도 된다. - 만족
- 특정 메서드는 로그를 출력하지 않는다 (`noLog()`) - 만족
- v1에는 적용할 수 있었지만 구체 클래스가 있는 v2, 컴포넌트 스캔을 활용하는 v3는 어떻게..? - 불만족

#### 구체 클래스를 이용한 프록시
```java
@Slf4j
public class TimeProxy extends ConcreteLogic {

    private ConcreteLogic concreteLogic;

    public TimeProxy(ConcreteLogic concreteLogic) {
        this.concreteLogic = concreteLogic;
    }

    @Override
    public String operation() {
        log.info("TimeDecorator 실행");
        LocalDateTime startTime = LocalDateTime.now();
        String result = concreteLogic.operation();
        return "[" + startTime + "] " + result;
    }
}
```
- 인터페이스 구현 대신 `클래스 상속`을 이용한다.
- 자바의 다형성은 인터페이스, 클래스를 구분하지 않고 모두 적용된다.
    - 해당 타입과 그 타입의 자식 타입은 모두 다형성의 대상이 된다.
- `ConcreteLogic concreteLogic`에는 `ConcreteLogic`을 상속받은 `TimeProxy`를 주입할 수 있다.
- `Client` -> `TimeProxy` -> `ConcreteLogic`

#### V2 프로젝트 기반 (v2 패키지 : 인터페이스가 없는 구체 클래스)
```java
public class OrderControllerConcreteProxy extends OrderControllerV2 {

    private final LogTrace logTrace;
    private final OrderControllerV2 target;

    public OrderControllerConcreteProxy(LogTrace logTrace, OrderControllerV2 target) {
        super(null);
        this.logTrace = logTrace;
        this.target = target;
    }

    @Override
    public String request(String itemId) {
        TraceStatus status = null;

        try {
            status = logTrace.begin("OrderController.request()");
            // target 호출
            String result = target.request(itemId);
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }

    @Override
    public String noLog() {
        return target.noLog();
    }
}
```
#### 클래스 기반 프록시의 단점 
- 자바 기본 문법에 의해 자식 클래스를 생성할 때는 항상 `super()`를 호출해야 한다. 
- 그래서 클래스 기반 프록시를 사용하게 되면 항상 `super(null..)`을 호출해줘야 한다.
- 인터페이스 기반 프록시는 이런걸 고민 안해도 된다.

#### 인터페이스 기반 프록시 vs 클래스 기반 프록시
- 인터페이스가 없어도 클래스 기반으로 프록시를 생성할 수 있는 것을 확인할 수 있다.
- 클래스 기반 프록시는 상속된 클래스에서만 적용할 수 있지만 인터페이스 기반 프록시는 인터페이스만 같으면 모든 곳에 적용할 수 있다. 
- 클래스 기반 프록시는 상속을 사용하기 때문에 제약이 있을 수 밖에 없다. 
    - 부모 클래스의 생성자 호출
    - 클래스에 final 키워드가 붙으면 상속 불가
    - 메서드에 final 키워드가 붙으면 오버라이딩 불가
- 인터페이스 기반 프록시는 꼭 인터페이스가 필요한 것이 유일한 단점이다. 

#### 그렇다면 인터페이스? 구체 클래스? 
- 이론적으로는 모든 객체에 인터페이스를 도입해서 역할과 구현을 나누는 것이 좋다. 
- 구현체를 매우 편리하게 변경할 수 있는 장점이 있다. 하지만 구현을 변경할 일이 없는 클래스도 많다. 
- 구현을 변경할 가능성이 거의 없는 코드에 무작정 인터페이스를 사용하는 것은 번거롭고 그렇게 실용적이지 않다.
- 즉, 무작정 쓸 필요는 없다는 것이다. 
- 실무에서는 인터페이스가 있거나 없는 경우가 혼재되어 있기 때문에 두가지 방법을 모두 알고 있는 것이 좋다.

#### 결론 
- 프록시를 활용함으로써 코드 변경 없이 로그 추적기라는 부가 기능을 적용하였다.