# 프록시
## 목차 
- [프록시](#프록시)
  - [목차](#목차)
  - [로그추적기 요구사항 추가](#로그추적기-요구사항-추가)
  - [프록시란?](#프록시란)
    - [대리자는 중간에서 여러 가지 일을 할 수 있다.](#대리자는-중간에서-여러-가지-일을-할-수-있다)
    - [대체 가능](#대체-가능)
  - [프록시 패턴 vs 데코레이터 패턴](#프록시-패턴-vs-데코레이터-패턴)
    - [프록시 패턴 캐싱 예제](#프록시-패턴-캐싱-예제)
    - [데코레이터 패턴](#데코레이터-패턴)
    - [프록시 패턴, 데코레이터 패턴의 문제점](#프록시-패턴-데코레이터-패턴의-문제점)
    - [결론](#결론)
  - [프록시 패턴과 데코레이터 패턴을 활용한 로그 추적기 구현](#프록시-패턴과-데코레이터-패턴을-활용한-로그-추적기-구현)
    - [V1 프로젝트 기반 (v1 패키지 : 인터페이스가 있는 구현 클래스)](#v1-프로젝트-기반-v1-패키지--인터페이스가-있는-구현-클래스)
    - [요구사항 만족 확인](#요구사항-만족-확인)
    - [구체 클래스를 이용한 프록시](#구체-클래스를-이용한-프록시)
    - [V2 프로젝트 기반 (v2 패키지 : 인터페이스가 없는 구체 클래스)](#v2-프로젝트-기반-v2-패키지--인터페이스가-없는-구체-클래스)
    - [클래스 기반 프록시의 단점](#클래스-기반-프록시의-단점)
    - [인터페이스 기반 프록시 vs 클래스 기반 프록시](#인터페이스-기반-프록시-vs-클래스-기반-프록시)
    - [그렇다면 인터페이스? 구체 클래스?](#그렇다면-인터페이스-구체-클래스)
    - [인터페이스? 구체클래스? 결론](#인터페이스-구체클래스-결론)
  - [동적 프록시](#동적-프록시)
    - [리플렉션](#리플렉션)
      - [리플렉션 활용 예제](#리플렉션-활용-예제)
    - [JDK 동적 프록시](#jdk-동적-프록시)
      - [동적 프록시란?](#동적-프록시란)
      - [JDK 동적 프록시 기본 예제 코드](#jdk-동적-프록시-기본-예제-코드)
      - [실행 순서](#실행-순서)
      - [결론](#결론-1)
      - [동적 프록시로 로그 추적기 개발](#동적-프록시로-로그-추적기-개발)
      - [JDK 동적 프록시 한계](#jdk-동적-프록시-한계)

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

### 대리자는 중간에서 여러 가지 일을 할 수 있다.
- 접근 제어, 캐싱
  - 권한에 따른 접근 차단
  - 캐싱 
  - 지연 로딩  
- 부가 기능 추가
  - 요청, 응답 값 변형
  - 실행시간 측정하여 추가 로깅 
- 프록시 체인 
  - 대리자가 또 다른 대리자를 부를 수 있다, 동생에게 라면을 사달라고 시켰더니 동생은 또 다시 다른 누군가에게 라면을 사오라고 시킬 수 있다. 중요한 점은 클라이언트는 대리자를 통해서 요청했기 때문에 그 이후의 과정은 모른다는 점이다. 이를 프록시 체인이라고 한다. 

### 대체 가능 
- 객체에서 프록시가 되려면 클라이언트는 서버에게 요청을 한 것인지, 프록시에게 요청을 한 것인지 조차 몰라야 한다.
- 쉽게 이야기해서 서버와 프록시는 같은 인터페이스를 사용해야 하며, 클라이언트가 사용하는 서버 객체를 프록시 객체로 변경해도 클라이언트 코드를 변경하지 않고 동작할 수 있어야 한다. 

## 프록시 패턴 vs 데코레이터 패턴 
- 둘다 프록시를 사용하는 방법이지만 GoF 디자인 패턴에서는 이 둘을 의도에 따라서 프록시 패턴과 데코레이터 패턴으로 구분한다.
- 프록시 패턴 : 접근 제어가 목적 
- 데코레이터 패턴 : 새로운 기능 추가가 목적

> ##### 참고 : 프록시라는 개념은 어디서든 사용된다. 규모의 차이가 있을 뿐 근본적인 역할은 같다.

### 프록시 패턴 캐싱 예제
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


### 데코레이터 패턴
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

### 프록시 패턴, 데코레이터 패턴의 문제점
<img width="852" alt="image" src="https://user-images.githubusercontent.com/44944031/164005790-5e5381d1-673c-478a-927a-3798ffe86238.png">

- 위의 프록시 패턴과 데코레이터 패턴을 살펴보면 문제점이 존재한다. `Decorator`들은 스스로 존재할 수 없다. 항상 꾸며줄 대상이 있어야 한다. 따라서 내부에 호출 대상인 `Component`가 존재해야 한다. 그리고 `Component`를 통해 항상 메서드를 호출해줘야 한다.
- 이런 중복을 제거하기 위해 위의 이미지와 같이 `Component`를 속성으로 가지고 있는 `Decorator`라는 추상 클래스를 만드는 방법도 고민할 수 있다. 
    - 이렇게 하면 추가로 얻을 수 있는 이점은 클래스 다이어그램에서 어떠한 것이 실제 컴포넌트 인지, 데코레이터인지 명확하게 구분할 수 있다. 

### 결론 
- 프록시 패턴과 데코레이터 패턴은 모양이 정말 비슷하다. 
- 다른 점은 결국 `의도(intent)` 이다. 디자인 패턴은 모양보단 의도가 정말로 중요하다. 
- 프록시 패턴과 데코레이터 패턴은 `의도`에 의해 구분된다.
> **프록시 패턴의 의도** : 다른 개체에 대한 `접근을 제어`하기 위해 데리자를 제공
> **데코레이터 패턴의 의도** : 객체에 `추가 책임을 동적으로 추가`하고, 기능 확장을 위한 유연한 대안 제공


## 프록시 패턴과 데코레이터 패턴을 활용한 로그 추적기 구현 

### V1 프로젝트 기반 (v1 패키지 : 인터페이스가 있는 구현 클래스)
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

### 요구사항 만족 확인 
- 원본 코드를 수정하지 않아도 된다. - 만족
- 특정 메서드는 로그를 출력하지 않는다 (`noLog()`) - 만족
- v1에는 적용할 수 있었지만 구체 클래스가 있는 v2, 컴포넌트 스캔을 활용하는 v3는 어떻게..? - 불만족

### 구체 클래스를 이용한 프록시
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

### V2 프로젝트 기반 (v2 패키지 : 인터페이스가 없는 구체 클래스)
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
### 클래스 기반 프록시의 단점 
- 자바 기본 문법에 의해 자식 클래스를 생성할 때는 항상 `super()`를 호출해야 한다. 
- 그래서 클래스 기반 프록시를 사용하게 되면 항상 `super(null..)`을 호출해줘야 한다.
- 인터페이스 기반 프록시는 이런걸 고민 안해도 된다.

### 인터페이스 기반 프록시 vs 클래스 기반 프록시
- 인터페이스가 없어도 클래스 기반으로 프록시를 생성할 수 있는 것을 확인할 수 있다.
- 클래스 기반 프록시는 상속된 클래스에서만 적용할 수 있지만 인터페이스 기반 프록시는 인터페이스만 같으면 모든 곳에 적용할 수 있다. 
- 클래스 기반 프록시는 상속을 사용하기 때문에 제약이 있을 수 밖에 없다. 
    - 부모 클래스의 생성자 호출
    - 클래스에 final 키워드가 붙으면 상속 불가
    - 메서드에 final 키워드가 붙으면 오버라이딩 불가
- 인터페이스 기반 프록시는 꼭 인터페이스가 필요한 것이 유일한 단점이다. 

### 그렇다면 인터페이스? 구체 클래스? 
- 이론적으로는 모든 객체에 인터페이스를 도입해서 역할과 구현을 나누는 것이 좋다. 
- 구현체를 매우 편리하게 변경할 수 있는 장점이 있다. 하지만 구현을 변경할 일이 없는 클래스도 많다. 
- 구현을 변경할 가능성이 거의 없는 코드에 무작정 인터페이스를 사용하는 것은 번거롭고 그렇게 실용적이지 않다.
- 즉, 무작정 쓸 필요는 없다는 것이다. 
- 실무에서는 인터페이스가 있거나 없는 경우가 혼재되어 있기 때문에 두가지 방법을 모두 알고 있는 것이 좋다.

### 인터페이스? 구체클래스? 결론 
- 프록시를 활용함으로써 코드 변경 없이 로그 추적기라는 부가 기능을 적용하였다.
- 문제는 프록시 클래스를 너무 많이 만들어야 한다. 그리고 프록시 클래스 내의 로직도 다 똑같다! 대상 클래스만 다르다.
- 클래스가 100개면 프록시 클래스도 100개가 필요하다. 

## 동적 프록시 
### 리플렉션
- 자바가 기본으로 제공하는 JDK 동적 프록시 기술이나 CGLIB 같은 프록시 생성 오픈소스 기술을 활용하면 프록시 객체를 동적으로 만들어낼 수 있다. 
- JDK 동적 프록시를 이해하기 위해서는 리플렉션 기술을 이해해야 한다.
- 리플렉션 기술을 사용하면 클래스나 메서드의 메타정보를 동적으로 획득하고, 코드도 동적으로 호출할 수 있다.

#### 리플렉션 활용 예제
```java
@Test
void reflection0() {
    Hello target = new Hello();

    //공통 로직1 시작
    log.info("start");
    String result1 = target.callA(); // 호출하는 메서드가 다름
    log.info("result={}", result1);

    //공통 로직2 시작
    log.info("start");
    String result2 = target.callB(); // 호출하는 메서드가 다름
    log.info("result={}", result2);
}
```

- 공통로직1과 공통로직2는 사실상 전체 코드 흐름이 동일하다. 메서드만 다르다. 
- 여기서 공통로직1과 공통로직2를 하나의 메서드로 뽑아서 합칠 수 있을까?
    - 호출하는 메서드가 다르기 때문에 공통화하는 것이 어렵다.
    - 호출하는 메서드인 `callA()`, `callB()`만 동적으로 처리할 수 있다면 공통화를 할 수 있다.

```java
void useLambda() {
    FunctionalInterface function1 = () -> "A";
    lambdaDynamicCall(function1);

    FunctionalInterface function2 = () -> "B";
    lambdaDynamicCall(function2);
}

private void lambdaDynamicCall(FunctionalInterface function) {
    log.info("start");
    String result = function.doSomething();
    log.info("result={}", result);
}
```
- 참고로 다음과 같이 람다로도 해결할 수 있긴 하다. 

```java
@Test
void reflection1() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // 클래스 정보
    Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");

    Hello target = new Hello();
    //callA 메서드 정보
    Method callA = classHello.getMethod("callA");
    Object result1 = callA.invoke(target);
    log.info("result={}", result1);

    //callB 메서드 정보
    Method callB = classHello.getMethod("callB");
    Object result2 = callB.invoke(target);
    log.info("result={}", result2);
}
```
- `classHello.getMethod("call")` 해당 클래스의 `call` 메서드 메타 정보 획득
- `callA.invoke(target)` 인스턴스의 메서드 호출
- 여기서의 핵심은 파라미터를 통해 가져올 클래스나 메서드 정보를 동적으로 수정할 수 있다.

```java
@Test
void reflection2() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // 클래스 정보
    Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");
    Hello target = new Hello();

    Method callA = classHello.getMethod("callA");
    dynamicCall(callA, target);

    Method callB = classHello.getMethod("callB");
    dynamicCall(callB, target);
}

private void dynamicCall(Method method, Object target) throws InvocationTargetException, IllegalAccessException {
    log.info("start");
    Object result = method.invoke(target);
    log.info("result={}", result);
}
```
- 정적인 `target.callA()`, `target.callB()` 코드를 리플렉션을 사용해서 `Method`라는 메타정보로 추상화했다.
- 리플렉션을 사용하면 클래스와 메서드의 메타정보를 사용하여 애플리케이션을 동적으로 유연하게 만들 수 있다.
- **주의)** 리플렉션 기술은 런타임에 동작하기 때문에 컴파일 시점에 오류를 잡을 수 없다.
  - 따라서 리플렉션은 일반적으로 활용하면 안된다. 리플렉션은 프레임워크 개발이나 또는 매우 일반적인 공통 처리가 필요할 때 부분적으로 주의해서 사용해야 한다.


### JDK 동적 프록시 
#### 동적 프록시란? 
- 클래스가 100개가 있으면 프록시 클래스 100개가 필요하다. 
- 그런데 프록시 클래스는 로직이 모두 동일하지만 대상만 다르다. 
- 이 문제를 해결하는 것이 바로 `동적 프록시 기술`이다.
  - 프록시 객체를 동적으로 런타임에 만들어주고, 동적 프록시에 원하는 실행 로직을 지정할 수 있다. 

#### JDK 동적 프록시 기본 예제 코드 
> ##### JDK 동적 프록시는 인터페이스 기반으로 프록시를 동적으로 만들어준다. 따라서 인터페이스가 필수이다. 

`InvocationHandler` 인터페이스 구현하여 작성
```java
@Slf4j
public class AImpl implements AInterface {
    @Override
    public String call() {
        log.info("A 호출");
        return "a";
    }
}
```
```java
@Slf4j
public class TimeInvocationHandler implements InvocationHandler {

    private final Object target;

    public TimeInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("TimeProxy 실행");
        long start = System.currentTimeMillis();

        Object result = method.invoke(target, args); // call()

        long end = System.currentTimeMillis();
        long resultTime = end - start;
        log.info("TimeProxy 종료 resultTime={}", resultTime);

        return result;
    }
}
```
- `Object target` : 동적 프록시가 호출할 대상 
- `method.invoke(target, args)` : 리플렉션을 사용해서 `target` 인스턴스의 메서드를 실행, args는 메서드 호출시 넘겨줄 인자이다.

```java
@Test
void dynamicA() {
    AInterface target = new AImpl();
    TimeInvocationHandler handler = new TimeInvocationHandler(target);

    AInterface proxy = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler);
    // 프록시가 사용할 로직은 handler이고, AInterface.class의 클래스 로더에 할꺼고 AInterface를 기반으로 프록시를 만들꺼야

    proxy.call();
    log.info("targetClass={}", target.getClass());
    log.info("proxyClass={}", proxy.getClass());
}
```
```text
20:17:24.727 [Test worker] INFO hello.proxy.jdkdynamic.JdkDynamicProxyTest - targetClass=class hello.proxy.jdkdynamic.code.AImpl
20:17:24.727 [Test worker] INFO hello.proxy.jdkdynamic.JdkDynamicProxyTest - proxyClass=class com.sun.proxy.$Proxy12
```
- `new TimeInvocationHandler(target)` : 동적 프록시에 적용할 핸들러 로직
- `(AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler)` 
  - 동적 프록시는 `java.lang.reflect.Proxy`를 통해서 생성할 수 있다.
  - 클래스 로더 정보, 인터페이스, 핸들러 로직을 인자로 넘겨주면 인터페이스를 기반으로 동적 프록시를 생성하고 그 결과를 반환한다.
  - 자바는 클래스가 호출이 되면 ClassLoader에 적재가 되는데, 그렇기 때문에 어디 클래스 로더에 적재할지 인자 정보로 넘겨줘야 한다.


#### 실행 순서 
<img width="851" alt="image" src="https://user-images.githubusercontent.com/44944031/164974782-7fe0a6ce-f8ea-4658-b055-a3fabfd9b943.png">

- 클라이언트는 JDK 동적 프록시의 `call()`을 실행 
- JDK 동적 프록시는 `invoke()`를 호출한다.
- `TimeInvocationHandler`가 내부 로직을 수행하고, `method.invoke()`를 통해서 `AImpl`의 로직을 실행한다. 
- `AImpl`의 `call()`이 실행되고, `TimeInvocationHandler`로 응답이 돌아온다.

#### 결론 
<img width="851" alt="image" src="https://user-images.githubusercontent.com/44944031/164974888-040fc4bb-f195-4b46-906c-96a3279d13a5.png">

<img width="848" alt="image" src="https://user-images.githubusercontent.com/44944031/164974892-84402fb5-d892-4e89-be43-8305f0b6a70a.png">

- 프록시 클래스를 수도 없이 만들어야 하는 문제를 해결할 필요가 없어졌다. 
- 각각 필요한 `InvocationHandler`만 만들어서 넣어주면 된다. 

#### 동적 프록시로 로그 추적기 개발 
- JDK 동적 프록시는 인터페이스가 필수이기 때문에 V1 애플리케이션에만 적용할 수 있다. 
```java
public class LogTraceFilterHandler implements InvocationHandler {

    private final Object target;
    private final LogTrace logTrace;
    private final String[] patterns;

    public LogTraceFilterHandler(Object target, LogTrace logTrace, String[] patterns) {
        this.target = target;
        this.logTrace = logTrace;
        this.patterns = patterns;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //메서드 이름 필터
        String methodName = method.getName();
        //save, request, reque*, *est
        if (!PatternMatchUtils.simpleMatch(patterns, methodName)) {
            return method.invoke(target, args);
        }

        TraceStatus status = null;

        try {
            String message = method.getDeclaringClass().getSimpleName() + "." +
                    method.getName() + "()";
            status = logTrace.begin(message);

            // target 호출
            Object result = method.invoke(target, args);
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
```
- `private final Object target` : 프록시가 호출할 대상
- `method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()"` : `Method`를 통해서 호출되는 메서드 정보와 클래스 정보를 동적으로 확인할 수 있음
- `!PatternMatchUtils.simpleMatch(patterns, methodName)` : `nolog()` 메서드가 호출되는 경우는 프록시 로직이 실행되지 않도록 제한하기 위해서 조건 추가

```java
@Configuration
public class DynamicProxyBasicConfig {

    private static final String[] PATTERNS = {"request*", "order*", "save*"};

    @Bean
    public OrderControllerV1 orderControllerV1(LogTrace logTrace) {
        OrderControllerV1 orderController = new OrderControllerV1Impl(orderServiceV1(logTrace));

        OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(
                OrderControllerV1.class.getClassLoader(),
                new Class[]{OrderControllerV1.class},
                new LogTraceFilterHandler(orderController, logTrace, PATTERNS));

        return proxy;
    }

    @Bean
    public OrderServiceV1 orderServiceV1(LogTrace logTrace) {
        OrderServiceV1 orderService = new OrderServiceV1Impl(orderRepositoryV1(logTrace));

        OrderServiceV1 proxy = (OrderServiceV1) Proxy.newProxyInstance(
                OrderServiceV1.class.getClassLoader(),
                new Class[]{OrderServiceV1.class},
                new LogTraceFilterHandler(orderService, logTrace, PATTERNS));
        return proxy;
    }

    @Bean
    public OrderRepositoryV1 orderRepositoryV1(LogTrace logTrace) {
        OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();

        OrderRepositoryV1 proxy = (OrderRepositoryV1) Proxy.newProxyInstance(
                OrderRepositoryV1.class.getClassLoader(),
                new Class[]{OrderRepositoryV1.class},
                new LogTraceFilterHandler(orderRepository, logTrace, PATTERNS));
        return proxy;
    }
}
```

<img width="854" alt="image" src="https://user-images.githubusercontent.com/44944031/164978580-611ffe8f-b697-411f-b0e0-3f8021ccfb68.png">

#### JDK 동적 프록시 한계 
- 인터페이스가 필수
- 인터페이스가 없는 경우는 `CGLIB`라는 바이트코드 조작 라이브러리를 활용해야 한다.