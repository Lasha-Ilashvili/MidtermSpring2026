package history.persistence.repository;

import history.persistence.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<RoundEntity, Long> {
}
