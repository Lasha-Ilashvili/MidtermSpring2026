package controller;

import game.GameCharacterizationTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CharacterizationSuiteTest {

    private static final AtomicInteger PASSED = new AtomicInteger();

    @Test
    void gameCharacterizationChecks() {
        PASSED.addAndGet(verified(new GameCharacterizationTests().run(), 63));
    }

    @Test
    void cliCharacterizationChecks() {
        PASSED.addAndGet(verified(new CliCharacterizationTests().run(), 1));
    }

    @Test
    void playerPromptCharacterizationChecks() {
        PASSED.addAndGet(verified(new PlayerPromptCharacterizationTests().run(), 8));
    }

    @Test
    void integrationCharacterizationChecks() {
        PASSED.addAndGet(verified(new IntegrationCharacterizationTests().run(), 3));
    }

    @AfterAll
    static void reportCharacterizationCheckCount() {
        assertEquals(75, PASSED.get());
        System.out.println("Passed " + PASSED.get() + " characterization checks.");
    }

    private static int verified(int actual, int expected) {
        assertEquals(expected, actual);
        return actual;
    }
}
