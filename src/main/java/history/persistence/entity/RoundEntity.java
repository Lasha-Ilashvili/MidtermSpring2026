package history.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "uno_rounds",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_round_number",
                columnNames = {"game_id", "round_number"}
        )
)
public class RoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoundStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_game_player_id")
    private GamePlayerEntity winner;

    @Column(name = "points_awarded", nullable = false)
    private int pointsAwarded;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RoundScoreEntity> scores = new ArrayList<>();

    protected RoundEntity() {
    }

    public RoundEntity(
            int roundNumber,
            Instant startedAt,
            Instant completedAt,
            RoundStatus status,
            GamePlayerEntity winner,
            int pointsAwarded
    ) {
        this.roundNumber = roundNumber;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.winner = winner;
        this.pointsAwarded = pointsAwarded;
    }

    public void addScore(RoundScoreEntity score) {
        scores.add(score);
        score.attachTo(this);
    }

    void attachTo(GameEntity game) {
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public GamePlayerEntity getWinner() {
        return winner;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public List<RoundScoreEntity> getScores() {
        return Collections.unmodifiableList(scores);
    }
}
