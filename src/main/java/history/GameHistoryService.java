package history;

import history.persistence.entity.GameEntity;
import history.persistence.entity.GamePlayerEntity;
import history.persistence.entity.PlayerEntity;
import history.persistence.entity.RoundEntity;
import history.persistence.entity.RoundScoreEntity;
import history.persistence.entity.RoundStatus;
import history.persistence.repository.GameRepository;
import history.persistence.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class GameHistoryService implements GameHistoryWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameHistoryService.class);

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;

    public GameHistoryService(PlayerRepository playerRepository, GameRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional
    public void save(CompletedGame completedGame) {
        GameEntity game = new GameEntity(
                completedGame.startedAt(),
                completedGame.completedAt(),
                completedGame.requestedRounds(),
                completedGame.completedRounds()
        );
        Map<String, GamePlayerEntity> gamePlayersByName = addPlayers(completedGame, game);
        addRounds(completedGame, game, gamePlayersByName);

        GameEntity saved = gameRepository.save(game);
        LOGGER.info(
                "event=history_saved game_id={} rounds={} players={}",
                saved.getId(),
                completedGame.completedRounds(),
                completedGame.players().size()
        );
    }

    private Map<String, GamePlayerEntity> addPlayers(CompletedGame completedGame, GameEntity game) {
        Map<String, GamePlayerEntity> gamePlayersByName = new LinkedHashMap<>();

        for (int index = 0; index < completedGame.players().size(); index++) {
            PlayerResult result = completedGame.players().get(index);
            PlayerEntity player = findOrCreatePlayer(result.name());
            GamePlayerEntity gamePlayer = new GamePlayerEntity(
                    player,
                    index,
                    result.finalScore(),
                    result.winner()
            );
            game.addPlayer(gamePlayer);
            gamePlayersByName.put(result.name(), gamePlayer);
        }
        return gamePlayersByName;
    }

    private void addRounds(
            CompletedGame completedGame,
            GameEntity game,
            Map<String, GamePlayerEntity> gamePlayersByName
    ) {
        for (CompletedRound completedRound : completedGame.rounds()) {
            GamePlayerEntity winner = completedRound.winnerName() == null
                    ? null
                    : requiredGamePlayer(gamePlayersByName, completedRound.winnerName());
            RoundEntity round = new RoundEntity(
                    completedRound.number(),
                    completedRound.startedAt(),
                    completedRound.completedAt(),
                    RoundStatus.valueOf(completedRound.status().name()),
                    winner,
                    completedRound.pointsAwarded()
            );

            for (PlayerScore score : completedRound.scores()) {
                round.addScore(new RoundScoreEntity(
                        requiredGamePlayer(gamePlayersByName, score.playerName()),
                        score.scoreBefore(),
                        score.scoreDelta(),
                        score.scoreAfter()
                ));
            }
            game.addRound(round);
        }
    }

    private PlayerEntity findOrCreatePlayer(String displayName) {
        String normalizedName = displayName.trim().toLowerCase(Locale.ROOT);
        return playerRepository.findByNormalizedName(normalizedName)
                .orElseGet(() -> playerRepository.save(new PlayerEntity(displayName, normalizedName)));
    }

    private GamePlayerEntity requiredGamePlayer(
            Map<String, GamePlayerEntity> gamePlayersByName,
            String playerName
    ) {
        GamePlayerEntity gamePlayer = gamePlayersByName.get(playerName);
        if (gamePlayer == null) {
            throw new IllegalArgumentException("Unknown completed-game player");
        }
        return gamePlayer;
    }
}
