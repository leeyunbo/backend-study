## 로그 추적기 예시
### 정상 요청
```text
[796bccd9] OrderController.request() 
[796bccd9] |-->OrderService.orderItem()
[796bccd9] |    |-->OrderRepository.save() 
[796bccd9] |    |<--OrderRepository.save() time=1004ms
[796bccd9] |<--OrderService.orderItem() time=1014ms 
[796bccd9] OrderController.request() time=1016ms
```
### 예외 발생
```text
[b7119f27] OrderController.request() 
[b7119f27] |-->OrderService.orderItem()
[b7119f27] |    |-->OrderRepository.save() 
[b7119f27] |    |<X-OrderRepository.save() time=0ms ex=java.lang.IllegalStateException: 예외 발생!
[b7119f27] |<X-OrderService.orderItem() time=10ms ex=java.lang.IllegalStateException: 예외 발생! 
[b7119f27] OrderController.request() time=11ms ex=java.lang.IllegalStateException: 예외 발생!
```

## 요구사항
- 모든 PUBLIC 메서드의 호출과 응답 정보를 로그로 출력 
- 애플리케이션의 흐름을 변경하면 안됨
    - 로그를 남긴다고 해서 비즈니스 로직의 동작에 영향을 주면 안됨 
- 메서드 호출에 걸린 시간
- 정상 흐름과 예외 흐름 구분
  - 예외 발생시 예외 정보가 남아야 함 
- 메서드 호출의 깊이 표현 
- HTTP 요청을 구분
  - HTTP 요청 단위로 특정 ID를 남겨서 어떤 HTTP 요청에서 시작된 것인지 명확하게 구분이 가능해야 함 
  - 트랜잭션 ID (DB 트랜잭션X), 여기서는 하나의 HTTP 요청이 시작해서 끝날 때 까지를 하나의 트랜잭션이라 함

## 프로젝트 버전 설명 
### V1 
- 단순히 로그 추적기를 주입받고 호출하는 수준
- try catch 문을 통한 Exception 로그 처리

#### V1 문제점
- 로그 status가 객체끼리 공유되지 않기 때문에, 깊이(화살표)를 구현하지 못함
- 트랜잭션 아이디가 공유되지 않음

<br/>

### V2
-  V1과 깊이 출력 기능까지 구현
-  단순히 파라미터에 TraceId를 넘겨줌으로써 다음 객체가 깊이를 표현할 수 있도록 함. 
-  트랜잭션 아이디도 공유 가능

#### V2 문제점 
- 파라미터로 다 넘겨주는데, 모든 메서드가 TraceId를 받아야 하는 문제.. (메서드를 다 고쳐야함)
  - 만약 인터페이스가 있다면, 인터페이스도 다 고쳐야함
  - 파라미터를 사용하지 않고 TraceId를 공유할 수 있는 방법은 없을까?

<br/>

###  V3 
- V2와 다르게 메서드에 파라미터를 추가할 필요가 없음 
- 싱글톤으로 생성되는 객체를 활용한 방식

#### V3 문제점
![image](https://user-images.githubusercontent.com/44944031/159718324-7162d387-8cbf-4a61-810f-240cd806d66d.png)
- 동시성 문제
  - 하나의 요청만 들어오는게 아니잖아
  - 쓰레드는 다른데 트랜잭션 ID는 똑같음 

#### 동시성 문제 원인 
- `FieldLogTrace`는 싱글톤으로 등록된 스프링 빈
- 여러 쓰레드가 동시에 접근하기 때문에 문제가 발생함
  - 동시성 문제는 지역 변수에서는 발생하지 않는다, 지역 변수는 쓰레드마다 각각 다른 메모리 영역이 할당되기 떄문이다. 
  - 동시성 문제는 같은 인스턴스의 필드 (주로 싱글톤), 또는 static 같은 공용 필드에 접근할 때 발생한다. 동시성 문제는 값을 읽기만 하면 발생하지 않고 쓰기를 할 경우에 발생하게 된다.

<br/>

### V4 - (V3의 동시성 문제 해결, ThreadLocal) 
- 해당 쓰레드만 접근할 수 있는 특별한 저장소를 의미한다. 
- 쓰레드 로컬을 사용하면 각 쓰레드마다 별도의 내부 저장소를 제공한다. 
- 따라서 같은 인스턴스의 쓰레드 로컬 필드에 접근해도 문제가 없다.
- 쓰레드 로컬을 모두 사용하고 나면 `ThreadLocal.remove()`를 호출하여 저장소를 초기화 해야만 한다.

#### 쓰레드 로컬 주의 사항
- 초기화를 안해줄 경우 WAS(TOMCAT)처럼 쓰레드 풀을 사용하는 경우에 심각한 문제가 발생할 수 있다.
- WAS는 쓰레드를 쓰레드 풀에 반환하고 재사용하게 되는데 초기화를 해주지 않으면 쓰레드 로컬의 데이터가 그대로 살아 있다.
- 따라서 꼭 `ThreadLocal.remove()`를 통해 쓰레드 로컬을 초기화해야 한다. 

#### V4 문제점 
- 각 비즈니스 로직마다 로그를 위한 로직이 중복됨
- 비즈니스 로직보다 로그를 위한 부가 기능 코드가 훨씬 더 많고 복잡함
```java
public void orderItem(TraceId traceId, String itemId) {
    TraceStatus status = null;
    try {
        status = trace.begin("OrderService.orderItem()"); // 반복
        orderRepository.save(itemId); // 비즈니스 로직
        trace.end(status); // 반복 
    } catch (Exception e) {
        trace.exception(status, e); // 반복
        throw e; // 반복
    }
}
```

 
<br/> 

### V5 - (V4의 중복 로직 문제 해결, 템플릿 메서드 패턴)
- V4의 문제점은 결국 부가 기능의 중복이고, 우리는 메인 기능과 부가 기능을 분리해서 모듈화를 진행해야만 해결할 수 있다. 
- 좋은 설계의 어플리케이션은 자주 변하는 부분과 변하지 않는 부분을 제대로 분리시킨 것
- 이러한 문제점을 해결할 수 있는 디자인 패턴이 바로 `템플릿 메서드 패턴`이다. 

#### 템플릿 메서드 패턴이란? 
<img width="643" alt="image" src="https://user-images.githubusercontent.com/44944031/160416827-b777045d-3d12-47f8-835c-47d6d7915038.png">

- 말 그대로 템플릿을 사용하는 방식
  - 여기서 템플릿은 기준이 되는 거대한 툴
  - 템플릿이라는 툴에 변하지 않는 로직을 몰아 넣고, 일부 변하는 부분을 따로 떼어내어 호출하는 방식으로 의존성을 해소한다.

#### 템플릿 메서드 패턴 - 추상클래스 및 상속과 오버라이딩 활용
```java
public abstract class AbstractTemplate {

    public void execute() {
        long startTime = System.currentTimeMillis();

        call(); // 상속
        
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime={}", resultTime);
    }

    protected abstract void call();
}
```
- 변하는 부분은 자식 클래스에 두고 상속과 오버라이딩을 사용하여 처리한다.
- 추상클래스 및 상속과 오버라이딩을 활용하게 되면 상속받는 클래스를 계속 만들어야 하는 단점이 있다.
- 이를 익명 내부 클래스를 활용하여 해결할 수 있다.

#### 템플릿 메서드 패턴 - 익명 내부 클래스 활용
```java
AbstractTemplate template1 = new AbstractTemplate() {
    @Override
    protected void call() {
        log.info("비즈니스 로직1 실행");
    }
};
template1.execute();

AbstractTemplate template2 = new AbstractTemplate() {
    @Override
    protected void call() {
        log.info("비즈니스 로직2 실행");
    }
};
template2.execute();
```
- 익명 내부 클래스를 활용하면 객체 인스턴스를 생성하여 생성할 클래스를 상속 받는 자식 클래스를 정의할 수 있다.
- 이름이 없고 클래스 내부에 선언되기 때문에 익명 내부 클래스라고 한다. 

#### 좋은 설계란? 
- 진정한 좋은 설계는 변경이 일어날 때 자연스럽게 드러난다. 
- 만약 로그를 남기는 로직을 변경해야 한다면? V5 같은 경우는 `AbstractTemplate` 코드만 변경하면 된다.
  - 즉, 단일 책임 원칙(SRP)를 지킨 것이다, 변경 지점을 하나로 모아서 변경에 쉽게 대처할 수 있는 구조를 만든 것이다.

#### 템플릿 메서드 패턴의 정의 

> ##### "작업에서 알고리즘의 골격을 정의하고 일부 단계를 하위 클래스로 연기한다, 템플릿 메서드를 사용하면 하위 클래스가 알고리즘의 구조를 변경하지 않고도 알고리즘의 특정 단계를 재정의할 수 있다."
-  변하지 않는 부분의 골격을 부모 클래스에 구현해놓고, 변경되는 단계는 하위 클래스에서 정의하는 것 
-  하지만 탬플릿 메서드 패턴은 상속을 사용하기 때문에 상속에서 오는 단점들을 그대로 안고간다.
     - 자식 클래스는 부모 클래스의 기능들을 사용하지 않지만 서로 연관되어 있다.
     - 사용하지도 않는데 부모 클래스의 모든 기능들을 상속받아 인지하고 있다.
     - 특정 클래스의 코드에 다른 클래스와 관련된 코드가 적혀있다는 것은 서로 강하게 의존한다는 것이다.
     - 즉, 부모 클래스가 변화한다면 자식 클래스에도 영향을 줄 수 밖에 없다.
-  비슷한 역할을 하면서 상속의 단점을 상쇄시킬 수 있는 것이 바로 전략 패턴이다.