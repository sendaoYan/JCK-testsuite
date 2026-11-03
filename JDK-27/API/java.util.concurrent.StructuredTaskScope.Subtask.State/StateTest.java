/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope$Subtask$State conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main StateTest
 */

import java.util.*;
import java.util.concurrent.*;

public class StateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope$Subtask$State");
        check(c.isEnum(), "Subtask.State is an enum");
        hasMethod(c, "values");
        hasMethod(c, "valueOf", String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StructuredTaskScope.Subtask.State[] all = StructuredTaskScope.Subtask.State.values();
        check(all.length == 3, "three states");
        check(StructuredTaskScope.Subtask.State.valueOf("UNAVAILABLE") ==
                StructuredTaskScope.Subtask.State.UNAVAILABLE, "valueOf UNAVAILABLE");
        check(StructuredTaskScope.Subtask.State.valueOf("SUCCESS") ==
                StructuredTaskScope.Subtask.State.SUCCESS, "valueOf SUCCESS");
        check(StructuredTaskScope.Subtask.State.valueOf("FAILED") ==
                StructuredTaskScope.Subtask.State.FAILED, "valueOf FAILED");
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.allUntil(sub -> true))) {
            StructuredTaskScope.Subtask<String> bad = scope.fork(() -> { throw new RuntimeException("boom"); });
            scope.join();
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED, "failed subtask reports FAILED");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
