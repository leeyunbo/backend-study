package hello.proxy.jdkdynamic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class ReflectionTest {

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

    @Slf4j
    static class Hello {
        public String callA() {
            log.info("callA");
            return "A";
        }

        public String callB() {
            log.info("callB");
            return "B";
        }
    }


    @Test
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
    
    public interface FunctionalInterface {
        String doSomething();
    }
}
