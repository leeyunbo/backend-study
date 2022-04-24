package hello.proxy;

import hello.proxy.config.AppConfigV1;
import hello.proxy.config.AppConfigV2;
import hello.proxy.config.v1_proxy.InterfaceProxyConfig;
import hello.proxy.config.v2_dynamicproxy.DynamicProxyBasicConfig;
import hello.proxy.trace.logtrace.LogTrace;
import hello.proxy.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

//@Import({AppConfigV1.class, AppConfigV2.class}) // config 패키지 하위는 컴포넌트 스캔 대상이 아니기 때문에 @Import를 활용해 빈으로 등록
//@Import(InterfaceProxyConfig.class)
@Import(DynamicProxyBasicConfig.class)
@SpringBootApplication(scanBasePackages = "hello.proxy.app") // app 패키지 하위 컴포넌트들만 스캔할 수 있도록 제한
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

	@Bean
	public LogTrace logTrace() {
		return new ThreadLocalLogTrace();
	}
}
