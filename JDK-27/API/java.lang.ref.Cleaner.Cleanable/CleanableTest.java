/*
 * @test
 * @summary java.lang.ref.Cleaner.Cleanable conformance: interface with clean().
 * @run main CleanableTest
 */
import java.lang.ref.Cleaner;

public class CleanableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Cleaner.Cleanable.class.isInterface(), "is interface");
        final boolean[] ran = {false};
        Cleaner.Cleanable c = () -> ran[0] = true;
        c.clean();
        check(ran[0], "clean() runs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
