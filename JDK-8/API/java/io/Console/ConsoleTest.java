/*
 * @test
 * @summary java.io.Console conformance: headless behavior.
 *          jtreg runs with redirected (non-tty) standard streams, so System.console()
 *          is null here: assert the headless contract (null on repeated calls,
 *          standard streams still present, no console object to operate on).
 * @run main ConsoleTest
 */

public class ConsoleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHeadless() {
        // No controlling terminal under jtreg: the only factory returns null.
        check(System.console() == null, "System.console() is null without a terminal");
        check(System.console() == null, "console stays null on repeated calls");
        // Standard streams remain available even though there is no Console.
        check(System.out != null && System.err != null && System.in != null,
              "standard streams still exist");
    }

    public static void main(String[] args) throws Exception {
        testHeadless();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
