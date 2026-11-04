/*
 * @test
 * @summary com.sun.activation.registries.MailcapParseException conformance: behavior of constructors, accessors and value semantics.
 * @run main MailcapParseExceptionTest
 */

public class MailcapParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.activation.registries.MailcapParseException e = new com.sun.activation.registries.MailcapParseException("bad mailcap");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check("bad mailcap".equals(e.getMessage()), "message");
        com.sun.activation.registries.MailcapParseException e2 = new com.sun.activation.registries.MailcapParseException();
        check(e2.getMessage() == null, "no-arg ctor message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
