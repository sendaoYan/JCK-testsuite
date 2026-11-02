/*
 * @test
 * @summary java.lang.ref.Cleaner conformance: create/register/clean.
 * @run main CleanerTest
 */
import java.lang.ref.Cleaner;

public class CleanerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        Cleaner cleaner = Cleaner.create();
        final boolean[] cleaned = {false};
        Object obj = new Object();
        Cleaner.Cleanable c = cleaner.register(obj, () -> cleaned[0] = true);
        check(c != null, "register non-null");
        c.clean();
        check(cleaned[0], "clean runs action");
        Cleaner c2 = Cleaner.create(r -> new Thread(r, "cleaner-test"));
        check(c2 != null, "create(ThreadFactory)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
