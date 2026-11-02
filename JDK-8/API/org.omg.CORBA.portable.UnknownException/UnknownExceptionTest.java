/*
 * @test
 * @summary org.omg.CORBA.portable.UnknownException conformance: concrete class behavior.
 * @run main UnknownExceptionTest
 */

public class UnknownExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.lang.Throwable t = new java.lang.Throwable("cause");
        org.omg.CORBA.portable.UnknownException ue = new org.omg.CORBA.portable.UnknownException(t);
        check(ue.originalEx == t, "originalEx stored");
        check(ue instanceof org.omg.CORBA.SystemException, "extends SystemException");
        check(ue.minor == 0, "minor default 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
