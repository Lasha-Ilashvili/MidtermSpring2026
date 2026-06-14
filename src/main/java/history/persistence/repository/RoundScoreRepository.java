package history.persistence.repository;

import history.persistence.entity.RoundScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundScoreRepository extends JpaRepository<RoundScoreEntity, Long> {
}
