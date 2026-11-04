/*
 * @test
 * @summary org.xml.sax.SAXException conformance: hierarchy, all constructors, nested-exception preservation, getMessage/getException/getCause/toString.
 * @run main SAXExceptionTest
 */

import org.xml.sax.SAXException;

public class SAXExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // ---- hierarchy ----
        check(Exception.class.isAssignableFrom(SAXException.class), "extends Exception");
        check(!RuntimeException.class.isAssignableFrom(SAXException.class), "is a checked exception");

        // ---- no-arg constructor ----
        SAXException e0 = new SAXException();
        check(e0.getMessage() == null, "no-arg getMessage null");
        check(e0.getException() == null, "no-arg getException null");
        check(e0.getCause() == null, "no-arg getCause null");
        check(e0.toString().contains("SAXException"), "no-arg toString is class name");

        // ---- message constructor ----
        SAXException e1 = new SAXException("boom");
        check("boom".equals(e1.getMessage()), "message constructor getMessage");
        check(e1.getException() == null, "message constructor getException null");
        check(e1.getCause() == null, "message constructor getCause null");
        check(e1.toString().contains("boom"), "message toString includes message");

        // ---- exception constructor (wraps) ----
        IllegalStateException cause = new IllegalStateException("root");
        SAXException e2 = new SAXException(cause);
        check(e2.getException() == cause, "exception constructor getException == original");
        check(e2.getCause() == cause, "getCause == original (wrapped)");
        check(cause.getMessage().equals(e2.getMessage()), "exception constructor message is cause message");

        // ---- message + exception constructor ----
        SAXException e3 = new SAXException("wrapped", cause);
        check(e3.getException() == cause, "message+exception getException == original");
        check(e3.getCause() == cause, "message+exception getCause == original");
        check("wrapped".equals(e3.getMessage()), "message+exception getMessage preserves custom message");
        check(e3.toString().contains("wrapped"), "message+exception toString includes message");

        // ---- null message constructor ----
        SAXException e4 = new SAXException((String) null);
        check(e4.getMessage() == null, "null message getMessage null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
