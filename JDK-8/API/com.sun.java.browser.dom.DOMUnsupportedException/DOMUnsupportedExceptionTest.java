/*
 * @test
 * @summary com.sun.java.browser.dom.DOMUnsupportedException conformance: message/cause semantics.
 * @run main DOMUnsupportedExceptionTest
 */

import com.sun.java.browser.dom.DOMUnsupportedException;

public class DOMUnsupportedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        DOMUnsupportedException e0 = new DOMUnsupportedException();
        check(e0 instanceof Exception, "extends Exception");
        check(e0.getMessage() == null, "no-arg message null");

        DOMUnsupportedException e1 = new DOMUnsupportedException("boom");
        check("boom".equals(e1.getMessage()), "string message");

        Exception cause = new Exception("cause");
        DOMUnsupportedException e2 = new DOMUnsupportedException(cause);
        check(cause == e2.getCause(), "cause");

        DOMUnsupportedException e3 = new DOMUnsupportedException(cause, "msg2");
        check("msg2".equals(e3.getMessage()), "cause+message message");
        check(cause == e3.getCause(), "cause+message cause");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
