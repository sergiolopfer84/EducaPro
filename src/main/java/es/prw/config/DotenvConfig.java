package es.prw.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

	@Bean
	public Dotenv dotenv() {
	    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	    System.out.println("DB_HOST: " + dotenv.get("DB_HOST"));
	    System.out.println("DB_PORT: " + dotenv.get("DB_PORT"));
	    System.out.println("DB_NAME: " + dotenv.get("DB_NAME"));
	    return dotenv;
	}
}
