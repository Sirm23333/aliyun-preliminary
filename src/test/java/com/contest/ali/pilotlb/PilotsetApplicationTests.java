package com.contest.ali.pilotlb;

import com.contest.ali.pilotlb.service.impl.iter1.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class PilotsetApplicationTests {

	@Test
	void contextLoads() {
	}
	public static void main(String[] args) throws CloneNotSupportedException {
		Service s1 = new Service();
		Service s2 = new Service();
		s1.setServiceName("abc");
		s2.setServiceName("abc");
		System.out.println(s1.equals(s2));
	}
}
