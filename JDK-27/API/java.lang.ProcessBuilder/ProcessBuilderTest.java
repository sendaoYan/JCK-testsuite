/*
 * @test
 * @summary java.lang.ProcessBuilder API conformance: start a command and wait for its exit
 *          code.
 * @run main ProcessBuilderTest
 */

public class ProcessBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testProcessBuilder() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("true");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        check(p.waitFor() == 0, "true exits 0");
    }

    public static void main(String[] args) throws Exception {
        testProcessBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
