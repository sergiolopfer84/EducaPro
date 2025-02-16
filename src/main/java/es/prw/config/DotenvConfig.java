package es.prw.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
    static {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        System.out.println("DB_HOST: " + System.getProperty("DB_HOST"));
        System.out.println("DB_PORT: " + System.getProperty("DB_PORT"));
        System.out.println("DB_NAME: " + System.getProperty("DB_NAME"));
        System.out.println("DB_USER: " + System.getProperty("DB_USER"));
        System.out.println("DB_PASSWORD: " + System.getProperty("DB_PASSWORD"));
    }

}