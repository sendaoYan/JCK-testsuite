/*
 * @test
 * @summary javax.swing.UnsupportedLookAndFeelException conformance: exception (headless-safe).
 * @run main UnsupportedLookAndFeelExceptionTest
 */

import javax.swing.UnsupportedLookAndFeelException;

public class UnsupportedLookAndFeelExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        UnsupportedLookAndFeelException e = new UnsupportedLookAndFeelException("x");
        check("x".equals(e.getMessage()), "message");
        check(e instanceof Exception, "is Exception");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
