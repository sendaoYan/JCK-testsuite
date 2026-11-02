/*
 * @test
 * @summary java.lang.StackWalker conformance: getInstance/walk/forEach/getCallerClass.
 * @run main StackWalkerTest
 */
import java.lang.StackWalker;

public class StackWalkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Class<?> caller() { return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass(); }
    public static void main(String[] args) {
        StackWalker sw = StackWalker.getInstance();
        long count = sw.walk(s -> s.count());
        check(count >= 1, "walk count");
        sw.forEach(f -> {});
        passed++;
        check(caller() == StackWalkerTest.class, "getCallerClass");
        check(StackWalker.getInstance(java.util.Set.of(StackWalker.Option.DROP_METHOD_INFO)) != null, "getInstance(Set)");
        check(StackWalker.getInstance(StackWalker.Option.SHOW_HIDDEN_FRAMES) != null, "getInstance(Option)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
