package history.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "round_scores",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_round_game_player",
                columnNames = {"round_id", "game_player_id"}
        )
)
public class RoundScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private RoundEntity round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_player_id", nullable = false)
    private GamePlayerEntity gamePlayer;

    @Column(name = "score_before", nullable = false)
    private int scoreBefore;

    @Column(name = "score_delta", nullable = false)
    private int scoreDelta;

    @Column(name = "score_after", nullable = false)
    private int scoreAfter;

    protected RoundScoreEntity() {
    }

    public RoundScoreEntity(GamePlayerEntity gamePlayer, int scoreBefore, int scoreDelta, int scoreAfter) {
        this.gamePlayer = gamePlayer;
        this.scoreBefore = scoreBefore;
        this.scoreDelta = scoreDelta;
        this.scoreAfter = scoreAfter;
    }

    void attachTo(RoundEntity round) {
        this.round = round;
    }

    public Long getId() {
        return id;
    }

    public GamePlayerEntity getGamePlayer() {
        return gamePlayer;
    }

    public int getScoreBefore() {
        return scoreBefore;
    }

    public int getScoreDelta() {
        return scoreDelta;
    }

    public int getScoreAfter() {
        return scoreAfter;
    }
}
