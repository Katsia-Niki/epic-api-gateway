package by.nikifarava.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=test-secret-key-test-secret-key-test-secret-key")
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
