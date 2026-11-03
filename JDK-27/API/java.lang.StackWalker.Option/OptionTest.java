/*
 * @test
 * @summary java.lang.StackWalker.Option conformance: white-box structure + black-box behavior.
 * @run main OptionTest
 */

import java.lang.StackWalker;

public class OptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(StackWalker.Option.class.isEnum(), "Option is an enum");
        check(Enum.class.isAssignableFrom(StackWalker.Option.class), "Option extends Enum");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConstants() {
        check(StackWalker.Option.RETAIN_CLASS_REFERENCE != null, "RETAIN_CLASS_REFERENCE");
        check(StackWalker.Option.DROP_METHOD_INFO != null, "DROP_METHOD_INFO");
        check(StackWalker.Option.SHOW_REFLECT_FRAMES != null, "SHOW_REFLECT_FRAMES");
        check(StackWalker.Option.SHOW_HIDDEN_FRAMES != null, "SHOW_HIDDEN_FRAMES");
        check(StackWalker.Option.values().length == 4, "four constants");
        check(StackWalker.Option.valueOf("DROP_METHOD_INFO") == StackWalker.Option.DROP_METHOD_INFO,
              "valueOf");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testConstants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
