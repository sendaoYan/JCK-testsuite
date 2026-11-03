/*
 * @test
 * @summary java.lang.StackWalker conformance: white-box structure + black-box behavior.
 * @run main StackWalkerTest
 */

import java.lang.reflect.Modifier;
import java.util.Set;

public class StackWalkerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StackWalker");
        check(Modifier.isFinal(c.getModifiers()), "StackWalker is final");
        check(!c.isInterface(), "StackWalker is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static Class<?> caller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
    }

    static void testWalk() {
        StackWalker sw = StackWalker.getInstance();
        long count = sw.walk(s -> s.count());
        check(count >= 1, "walk counts at least one frame");
        boolean[] sawMain = {false};
        sw.walk(s -> {
            s.forEach(f -> { if ("main".equals(f.getMethodName())) sawMain[0] = true; });
            return null;
        });
        check(sawMain[0], "walk sees the calling main frame");
    }

    static void testForEach() {
        StackWalker.getInstance().forEach(f -> { /* no-op */ });
        passed++;
    }

    static void testGetCallerClass() {
        check(caller() == StackWalkerTest.class, "getCallerClass returns this class");
    }

    static void testInstances() {
        check(StackWalker.getInstance(StackWalker.Option.DROP_METHOD_INFO) != null,
              "getInstance(Option)");
        check(StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE)) != null,
              "getInstance(Set)");
        check(StackWalker.getInstance(Set.of(), 32) != null, "getInstance(Set, estimate>0)");
        try {
            StackWalker.getInstance(Set.of(), 0);
            throw new AssertionError("getInstance(Set, 0): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWalk();
        testForEach();
        testGetCallerClass();
        testInstances();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
