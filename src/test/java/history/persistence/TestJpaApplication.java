package history.persistence;

import history.persistence.entity.GameEntity;
import history.persistence.repository.GameRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackageClasses = GameEntity.class)
@EnableJpaRepositories(basePackageClasses = GameRepository.class)
public class TestJpaApplication {
}
