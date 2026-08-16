package com.fivetuat.fsoftprojectserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class FsoftProjectServerApplicationTests {

	static {
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		System.setProperty("user.timezone", "Asia/Ho_Chi_Minh");
	}

	@Test
	void contextLoads() {
	}

}
