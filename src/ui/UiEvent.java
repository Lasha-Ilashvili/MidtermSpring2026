package ui;

public sealed interface UiEvent permits UiEvent.TextInput {

    record TextInput(String value) implements UiEvent {
    }
}
