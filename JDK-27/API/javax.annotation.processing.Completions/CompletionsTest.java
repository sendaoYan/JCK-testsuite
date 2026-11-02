/*
 * @test
 * @summary javax.annotation.processing.Completions conformance: completion factory.
 * @run main CompletionsTest
 */

public class CompletionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.annotation.processing.Completion c1 = javax.annotation.processing.Completions.of("value");
        check(c1 != null, "of(String) non-null");
        check("value".equals(c1.getValue()), "getValue");
        check("".equals(c1.getMessage()), "empty message");
        javax.annotation.processing.Completion c2 = javax.annotation.processing.Completions.of("v", "msg");
        check("v".equals(c2.getValue()), "getValue 2");
        check("msg".equals(c2.getMessage()), "getMessage 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
