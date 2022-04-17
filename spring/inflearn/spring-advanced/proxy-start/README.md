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