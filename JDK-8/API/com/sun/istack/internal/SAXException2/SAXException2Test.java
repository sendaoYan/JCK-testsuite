/*
 * @test
 * @summary com.sun.istack.internal.SAXException2 conformance: behavior of constructors, accessors and value semantics.
 * @run main SAXException2Test
 */

public class SAXException2Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.lang.Exception cause = new java.lang.Exception("cause");
        com.sun.istack.internal.SAXException2 e = new com.sun.istack.internal.SAXException2("msg", cause);
        check(e instanceof org.xml.sax.SAXException, "extends org.xml.sax.SAXException");
        check("msg".equals(e.getMessage()), "message");
        check(e.getCause() == cause, "cause");
        com.sun.istack.internal.SAXException2 e2 = new com.sun.istack.internal.SAXException2("only");
        check("only".equals(e2.getMessage()), "message only");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
