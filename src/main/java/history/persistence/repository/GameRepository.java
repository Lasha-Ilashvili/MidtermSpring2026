package history.persistence.repository;

import history.persistence.entity.GameEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<GameEntity, Long> {

    List<GameEntity> findAllByOrderByCompletedAtDesc(Pageable pageable);
}
