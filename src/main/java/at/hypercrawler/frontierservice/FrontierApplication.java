package at.hypercrawler.frontierservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FrontierApplication {

  public static void main(String[] args) {
    SpringApplication.run(FrontierApplication.class, args);
  }


}
