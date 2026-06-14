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
        name = "game_players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_game_player_order", columnNames = {"game_id", "player_order"}),
                @UniqueConstraint(name = "uk_game_player", columnNames = {"game_id", "player_id"})
        }
)
public class GamePlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Column(name = "player_order", nullable = false)
    private int playerOrder;

    @Column(name = "final_score", nullable = false)
    private int finalScore;

    @Column(name = "winner", nullable = false)
    private boolean winner;

    protected GamePlayerEntity() {
    }

    public GamePlayerEntity(PlayerEntity player, int playerOrder, int finalScore, boolean winner) {
        this.player = player;
        this.playerOrder = playerOrder;
        this.finalScore = finalScore;
        this.winner = winner;
    }

    void attachTo(GameEntity game) {
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public int getPlayerOrder() {
        return playerOrder;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public boolean isWinner() {
        return winner;
    }
}
