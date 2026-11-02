/*
 * @test
 * @summary java.lang.StackWalker.Option conformance: enum constants.
 * @run main OptionTest
 */
import java.lang.StackWalker;

public class OptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(StackWalker.Option.class.isEnum(), "is enum");
        check(StackWalker.Option.RETAIN_CLASS_REFERENCE != null, "RETAIN_CLASS_REFERENCE");
        check(StackWalker.Option.DROP_METHOD_INFO != null, "DROP_METHOD_INFO");
        check(StackWalker.Option.SHOW_REFLECT_FRAMES != null, "SHOW_REFLECT_FRAMES");
        check(StackWalker.Option.SHOW_HIDDEN_FRAMES != null, "SHOW_HIDDEN_FRAMES");
        check(StackWalker.Option.values().length == 4, "four constants");
        check(StackWalker.Option.valueOf("DROP_METHOD_INFO") == StackWalker.Option.DROP_METHOD_INFO, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
