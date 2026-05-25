package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SelfTestSupport {

    private SelfTestSupport() {
    }

    static int check(boolean condition, String name) {
        if (!condition) {
            fail(name);
        }
        return 1;
    }

    static List<String> cards(String... values) {
        ArrayList<String> result = new ArrayList<>();
        Collections.addAll(result, values);
        return result;
    }

    private static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
