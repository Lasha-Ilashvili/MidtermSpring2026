package history.persistence.repository;

import history.persistence.entity.GamePlayerEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, Long> {

    long countByPlayerNormalizedNameAndWinnerTrue(String normalizedName);

    @Query("""
            select gamePlayer
            from GamePlayerEntity gamePlayer
            join fetch gamePlayer.player
            join fetch gamePlayer.game
            order by gamePlayer.finalScore desc, gamePlayer.game.completedAt desc
            """)
    List<GamePlayerEntity> findHighestScores(Pageable pageable);
}
