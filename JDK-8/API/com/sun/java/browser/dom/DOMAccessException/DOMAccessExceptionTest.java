/*
 * @test
 * @summary com.sun.java.browser.dom.DOMAccessException conformance: message/cause semantics.
 * @run main DOMAccessExceptionTest
 */

import com.sun.java.browser.dom.DOMAccessException;

public class DOMAccessExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        DOMAccessException e0 = new DOMAccessException();
        check(e0 instanceof Exception, "extends Exception");
        check(e0.getMessage() == null, "no-arg message null");

        DOMAccessException e1 = new DOMAccessException("boom");
        check("boom".equals(e1.getMessage()), "string message");

        Exception cause = new Exception("cause");
        DOMAccessException e2 = new DOMAccessException(cause);
        check(cause == e2.getCause(), "cause");

        DOMAccessException e3 = new DOMAccessException(cause, "msg2");
        check("msg2".equals(e3.getMessage()), "cause+message message");
        check(cause == e3.getCause(), "cause+message cause");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
