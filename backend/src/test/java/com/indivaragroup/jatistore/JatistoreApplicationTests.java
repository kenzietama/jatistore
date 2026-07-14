package com.indivaragroup.jatistore;

import com.indivaragroup.jatistore.data.RequestVariable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class JatistoreApplicationTests {

	@Test
	void contextLoads() {
		RequestVariable requestVariable = new RequestVariable();
		assertNotNull(requestVariable);
	}

}
