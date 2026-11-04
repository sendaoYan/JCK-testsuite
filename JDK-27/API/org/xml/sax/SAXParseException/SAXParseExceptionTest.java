/*
 * @test
 * @summary org.xml.sax.SAXParseException conformance: hierarchy, all constructors, line/column/systemId/publicId accessors, toString.
 * @run main SAXParseExceptionTest
 */

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SAXParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Locator loc(final String pub, final String sys, final int line, final int col) {
        return new Locator() {
            public String getPublicId() { return pub; }
            public String getSystemId() { return sys; }
            public int getLineNumber() { return line; }
            public int getColumnNumber() { return col; }
        };
    }

    public static void main(String[] args) {
        // ---- hierarchy ----
        check(SAXException.class.isAssignableFrom(SAXParseException.class), "extends SAXException");

        // ---- (String, Locator) ----
        SAXParseException e1 = new SAXParseException("msg", loc("PUB", "SYS", 12, 34));
        check("msg".equals(e1.getMessage()), "message preserved");
        check("PUB".equals(e1.getPublicId()), "publicId from locator");
        check("SYS".equals(e1.getSystemId()), "systemId from locator");
        check(e1.getLineNumber() == 12, "lineNumber from locator");
        check(e1.getColumnNumber() == 34, "columnNumber from locator");
        check(e1.getException() == null, "no wrapped exception");
        check(e1.toString().contains("msg"), "toString includes message");

        // ---- (String, Locator, Exception) ----
        IllegalStateException cause = new IllegalStateException("root");
        SAXParseException e2 = new SAXParseException("msg2", loc("P", "S", 5, 6), cause);
        check(e2.getException() == cause, "wrapped exception preserved");
        check(e2.getCause() == cause, "getCause == wrapped");
        check(e2.getLineNumber() == 5 && e2.getColumnNumber() == 6, "position from locator + exception");

        // ---- (String, String, String, int, int) ----
        SAXParseException e3 = new SAXParseException("msg3", "PUB3", "SYS3", 7, 8);
        check("PUB3".equals(e3.getPublicId()), "publicId from string args");
        check("SYS3".equals(e3.getSystemId()), "systemId from string args");
        check(e3.getLineNumber() == 7 && e3.getColumnNumber() == 8, "line/column from int args");
        check("msg3".equals(e3.getMessage()), "message from string args");

        // ---- (String, String, String, int, int, Exception) ----
        SAXParseException e4 = new SAXParseException("msg4", "PUB4", "SYS4", 9, 10, cause);
        check(e4.getException() == cause, "wrapped exception from 6-arg ctor");
        check(e4.getSystemId().equals("SYS4") && e4.getLineNumber() == 9 && e4.getColumnNumber() == 10, "6-arg position");
        check(e4.toString().contains("msg4"), "6-arg toString includes message");
        check(e4.toString().contains("SYS4"), "toString includes systemId");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
