/*
 * @test
 * @summary java.lang.ThreadDeath API conformance: it is an Error.
 * @run main ThreadDeathTest
 */

public class ThreadDeathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadDeath() {
        ThreadDeath td = new ThreadDeath();
        check(td instanceof Error, "ThreadDeath is an Error");
        check(td instanceof Throwable, "ThreadDeath is a Throwable");
    }

    public static void main(String[] args) {
        testThreadDeath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
