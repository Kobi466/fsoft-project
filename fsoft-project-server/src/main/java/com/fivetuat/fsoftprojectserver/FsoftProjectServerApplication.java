package com.fivetuat.fsoftprojectserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class FsoftProjectServerApplication {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		System.setProperty("user.timezone", "Asia/Ho_Chi_Minh");
	}

	public static void main(String[] args) {
		SpringApplication.run(FsoftProjectServerApplication.class, args);
	}

}
