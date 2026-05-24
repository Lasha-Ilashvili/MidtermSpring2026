package ui;

import java.util.Objects;

public sealed interface UiType permits UiType.Cli {

    record Cli(String[] args) implements UiType {

        public Cli {
            args = Objects.requireNonNull(args).clone();
        }

        @Override
        public String[] args() {
            return args.clone();
        }
    }
}
