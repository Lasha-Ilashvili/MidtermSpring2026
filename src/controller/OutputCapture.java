package controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

final class OutputCapture {

    private String value = "";

    <T> T capture(Supplier<T> action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            return action.get();
        } finally {
            System.setOut(originalOut);
            value = output.toString(StandardCharsets.UTF_8);
        }
    }

    boolean contains(String text) {
        return value.contains(text);
    }
}
