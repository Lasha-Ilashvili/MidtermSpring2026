package application;

import controller.UnoGameController;
import history.persistence.entity.GameEntity;
import history.persistence.repository.GameRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import ui.UiType;

@SpringBootApplication(scanBasePackages = {"application", "history"})
@EntityScan(basePackageClasses = GameEntity.class)
@EnableJpaRepositories(basePackageClasses = GameRepository.class)
public class UnoApplication {

    public static void run(String[] args) {
        SpringApplication.run(UnoApplication.class, args);
    }

    @Bean
    ApplicationRunner gameRunner() {
        return (ApplicationArguments arguments) ->
                UnoGameController.startNewGame(new UiType.Cli(arguments.getSourceArgs()));
    }
}
