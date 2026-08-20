package by.nikifarava.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.services.auth-uri=http://localhost:8083")
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
