package history.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "uno_games")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "requested_rounds", nullable = false)
    private int requestedRounds;

    @Column(name = "completed_rounds", nullable = false)
    private int completedRounds;

    @Column(name = "target_score")
    private Integer targetScore;

    @Column(name = "completion_reason", nullable = false, length = 30)
    private String completionReason;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("playerOrder ASC")
    private List<GamePlayerEntity> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC")
    private List<RoundEntity> rounds = new ArrayList<>();

    protected GameEntity() {
    }

    public GameEntity(Instant startedAt, Instant completedAt, int requestedRounds, int completedRounds) {
        this(startedAt, completedAt, requestedRounds, completedRounds, null, "ROUND_CAP");
    }

    public GameEntity(
            Instant startedAt,
            Instant completedAt,
            int requestedRounds,
            int completedRounds,
            Integer targetScore,
            String completionReason
    ) {
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.requestedRounds = requestedRounds;
        this.completedRounds = completedRounds;
        this.targetScore = targetScore;
        this.completionReason = completionReason;
    }

    public void addPlayer(GamePlayerEntity player) {
        players.add(player);
        player.attachTo(this);
    }

    public void addRound(RoundEntity round) {
        rounds.add(round);
        round.attachTo(this);
    }

    public Long getId() {
        return id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public int getRequestedRounds() {
        return requestedRounds;
    }

    public int getCompletedRounds() {
        return completedRounds;
    }

    public Integer getTargetScore() {
        return targetScore;
    }

    public String getCompletionReason() {
        return completionReason;
    }

    public List<GamePlayerEntity> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<RoundEntity> getRounds() {
        return Collections.unmodifiableList(rounds);
    }
}
