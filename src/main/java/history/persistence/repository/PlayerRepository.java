package history.persistence.repository;

import history.persistence.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findByNormalizedName(String normalizedName);
}
