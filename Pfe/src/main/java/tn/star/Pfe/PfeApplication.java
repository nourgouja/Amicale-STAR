package tn.star.Pfe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PfeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PfeApplication.class, args);
		System.out.println("JWT_SECRET = " + System.getenv("JWT_SECRET"));
	}


}
