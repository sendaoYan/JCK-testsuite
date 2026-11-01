/*
 * @test
 * @summary java.lang.ProcessBuilder.Redirect.Type API conformance: enum constants.
 * @run main RedirectTypeTest
 */

import java.lang.ProcessBuilder.Redirect.Type;

public class RedirectTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRedirectType() {
        check(Type.INHERIT != null, "INHERIT exists");
        check(Type.PIPE != null, "PIPE exists");
        check(Type.READ != null, "READ exists");
        check(Type.WRITE != null, "WRITE exists");
        check(Type.valueOf("PIPE") == Type.PIPE, "valueOf");
    }

    public static void main(String[] args) {
        testRedirectType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
