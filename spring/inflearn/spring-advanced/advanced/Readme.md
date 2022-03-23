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
1. 단순히 로그 추적기를 주입받고 호출하는 수준
2. try catch 문을 통한 Exception 로그 처리

#### V1 문제점
1. 로그 status가 객체끼리 공유되지 않기 때문에, 깊이(화살표)를 구현하지 못함
2. 트랜잭션 아이디가 공유되지 않음

### V2
1. V1과 깊이 출력 기능까지 구현
2. 단순히 파라미터에 TraceId를 넘겨줌으로써 다음 객체가 깊이를 표현할 수 있도록 함. 
3. 트랜잭션 아이디도 공유 가능

#### V2 문제점 
1. 파라미터로 다 넘겨주는데, 모든 메서드가 TraceId를 받아야 하는 문제.. (메서드를 다 고쳐야함)
2. 만약 인터페이스가 있다면, 인터페이스도 다 고쳐야함
3. 파라미터를 사용하지 않고 TraceId를 공유할 수 있는 방법은 없을까?

###  V3 
1. V2와 다르게 메서드에 파라미터를 추가할 필요가 없음 
2. 싱글톤으로 생성되는 객체를 활용한 방식

#### V3 문제점
![image](https://user-images.githubusercontent.com/44944031/159718324-7162d387-8cbf-4a61-810f-240cd806d66d.png)
1. 동시성 문제
2. 하나의 요청만 들어오는게 아니잖아
3. 쓰레드는 다른데 트랜잭션 ID는 똑같음 

#### 동시성 문제 원인 
1. `FieldLogTrace`는 싱글톤으로 등록된 스프링 빈
2. 여러 쓰레드가 동시에 접근하기 때문에 문제가 발생함
3. 동시성 문제는 지역 변수에서는 발생하지 않는다, 지역 변수는 쓰레드마다 각각 다른 메모리 영역이 할당되기 떄문이다. 
4. 동시성 문제는 같은 인스턴스의 필드 (주로 싱글톤), 또는 static 같은 공용 필드에 접근할 때 발생한다. 동시성 문제는 값을 읽기만 하면 발생하지 않고 쓰기를 할 경우에 발생하게 된다.
