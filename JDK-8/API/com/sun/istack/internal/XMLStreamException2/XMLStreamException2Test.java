/*
 * @test
 * @summary com.sun.istack.internal.XMLStreamException2 conformance: behavior of constructors, accessors and value semantics.
 * @run main XMLStreamException2Test
 */

public class XMLStreamException2Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.lang.Exception cause = new java.lang.Exception("cause");
        com.sun.istack.internal.XMLStreamException2 e = new com.sun.istack.internal.XMLStreamException2("msg", cause);
        check(e instanceof javax.xml.stream.XMLStreamException, "extends javax.xml.stream.XMLStreamException");
        check("msg".equals(e.getMessage()), "message");
        check(e.getCause() == cause, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
