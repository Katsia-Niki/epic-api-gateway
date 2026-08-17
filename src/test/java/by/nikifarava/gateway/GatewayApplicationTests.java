package by.nikifarava.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.jwt.secret=test-secret-key-test-secret-key-test-secret-key",
							 "app.services.internal-key=test-internal-key"})
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
