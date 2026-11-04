/*
 * @test
 * @summary javax.swing.text.BadLocationException conformance: exception (headless-safe).
 * @run main BadLocationExceptionTest
 */

import javax.swing.text.BadLocationException;

public class BadLocationExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        BadLocationException e = new BadLocationException("msg", 3);
        check("msg".equals(e.getMessage()), "message");
        check(e.offsetRequested() == 3, "offset");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
