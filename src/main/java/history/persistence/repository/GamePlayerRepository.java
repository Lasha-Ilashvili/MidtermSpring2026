package history.persistence.repository;

import history.persistence.entity.GamePlayerEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GamePlayerRepository extends JpaRepository<GamePlayerEntity, Long> {

    long countByPlayerNormalizedNameAndWinnerTrue(String normalizedName);

    @Query("""
            select
                gamePlayer.game.id as gameId,
                gamePlayer.player.displayName as playerName,
                gamePlayer.finalScore as score,
                gamePlayer.game.completedAt as completedAt
            from GamePlayerEntity gamePlayer
            order by
                gamePlayer.finalScore desc,
                gamePlayer.game.completedAt desc,
                gamePlayer.game.id desc,
                gamePlayer.playerOrder asc
            """)
    List<HighestScoreProjection> findHighestScores(Pageable pageable);
}
