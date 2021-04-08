package basecamp.project.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
 * Setze main-Methode mit SpringBootApplication.
 */
@SpringBootApplication
public class ServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}