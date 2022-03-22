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

### V1 
1. 단순히 로그 추적기를 주입받고 호출하는 수준
2. try catch 문을 통한 Exception 로그 처리
3. 로그 status가 객체끼리 공유되지 않기 때문에, 깊이를 구현하지 못함