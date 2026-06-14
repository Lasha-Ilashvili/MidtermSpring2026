package history;

@FunctionalInterface
public interface GameHistoryWriter {

    void save(CompletedGame game);

    static GameHistoryWriter noOp() {
        return game -> {
        };
    }
}
