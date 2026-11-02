/*
 * @test
 * @summary javax.annotation.processing.Completions conformance: completions factory.
 * @run main CompletionsTest
 */


public class CompletionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.annotation.processing.Completion c = javax.annotation.processing.Completions.of("value", "note");
        check("value".equals(c.getValue()), "value");
        check("note".equals(c.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
