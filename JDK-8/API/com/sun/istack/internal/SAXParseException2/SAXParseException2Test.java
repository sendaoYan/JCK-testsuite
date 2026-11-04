/*
 * @test
 * @summary com.sun.istack.internal.SAXParseException2 conformance: behavior of constructors, accessors and value semantics.
 * @run main SAXParseException2Test
 */

public class SAXParseException2Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.lang.Exception cause = new java.lang.Exception("cause");
        com.sun.istack.internal.SAXParseException2 e = new com.sun.istack.internal.SAXParseException2("msg", "pub", "sys", 7, 9, cause);
        check(e instanceof org.xml.sax.SAXParseException, "extends org.xml.sax.SAXParseException");
        check("msg".equals(e.getMessage()), "message");
        check(e.getLineNumber() == 7, "line number");
        check(e.getColumnNumber() == 9, "column number");
        check(e.getCause() == cause, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
