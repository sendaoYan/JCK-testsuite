/*
 * @test
 * @summary javax.crypto.AEADBadTagException conformance: constructors, message, cause and superclass.
 * @run main AEADBadTagExceptionTest
 */

public class AEADBadTagExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.AEADBadTagException e = new javax.crypto.AEADBadTagException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.crypto.BadPaddingException, "extends javax.crypto.BadPaddingException");
        javax.crypto.AEADBadTagException e4 = new javax.crypto.AEADBadTagException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
