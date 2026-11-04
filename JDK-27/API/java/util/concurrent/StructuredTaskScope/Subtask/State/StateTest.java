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

    // ---- Part 3: contract deep-dive ----
    static void testEnumContract() {
        StructuredTaskScope.Subtask.State[] all = StructuredTaskScope.Subtask.State.values();
        check(all[0] == StructuredTaskScope.Subtask.State.UNAVAILABLE
                && all[1] == StructuredTaskScope.Subtask.State.SUCCESS
                && all[2] == StructuredTaskScope.Subtask.State.FAILED,
                "declaration order UNAVAILABLE, SUCCESS, FAILED");
        check("UNAVAILABLE".equals(StructuredTaskScope.Subtask.State.UNAVAILABLE.name()), "name UNAVAILABLE");
        check("SUCCESS".equals(StructuredTaskScope.Subtask.State.SUCCESS.name()), "name SUCCESS");
        check("FAILED".equals(StructuredTaskScope.Subtask.State.FAILED.name()), "name FAILED");
        check(StructuredTaskScope.Subtask.State.UNAVAILABLE.ordinal() == 0, "ordinal UNAVAILABLE == 0");
        check(StructuredTaskScope.Subtask.State.SUCCESS.ordinal() == 1, "ordinal SUCCESS == 1");
        check(StructuredTaskScope.Subtask.State.FAILED.ordinal() == 2, "ordinal FAILED == 2");
        // Enum contracts
        check(StructuredTaskScope.Subtask.State.SUCCESS.getDeclaringClass()
                  == StructuredTaskScope.Subtask.State.class, "getDeclaringClass");
        check(java.util.EnumSet.allOf(StructuredTaskScope.Subtask.State.class).size() == 3,
                "EnumSet sees all three constants");
        try {
            StructuredTaskScope.Subtask.State.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown constant throws IllegalArgumentException");
        }
    }

    static void testStatesObserved() throws Exception {
        // SUCCESS and FAILED observed after join; UNAVAILABLE observed for a cancelled subtask.
        // The predicate stays false so the scope waits for ALL subtasks (deterministic states).
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allUntil(sub -> false))) {
            StructuredTaskScope.Subtask<String> good = scope.fork(() -> "g");
            StructuredTaskScope.Subtask<String> bad = scope.fork(() -> { throw new IllegalStateException("b"); });
            scope.join();
            check(good.state() == StructuredTaskScope.Subtask.State.SUCCESS, "completed subtask SUCCESS");
            check(bad.state() == StructuredTaskScope.Subtask.State.FAILED, "thrown subtask FAILED");
        }
        try (StructuredTaskScope<String, List<StructuredTaskScope.Subtask<String>>, RuntimeException> scope =
                 StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>allUntil(
                         sub -> sub.state() == StructuredTaskScope.Subtask.State.SUCCESS))) {
            scope.fork(() -> "trigger");
            StructuredTaskScope.Subtask<String> slow = scope.fork(() -> {
                Thread.sleep(30_000);
                return "slow";
            });
            scope.join();
            check(slow.state() == StructuredTaskScope.Subtask.State.UNAVAILABLE,
                    "subtask cancelled before completion reports UNAVAILABLE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumContract();
        testStatesObserved();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
