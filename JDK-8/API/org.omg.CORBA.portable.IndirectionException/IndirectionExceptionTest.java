/*
 * @test
 * @summary org.omg.CORBA.portable.IndirectionException conformance: concrete class behavior.
 * @run main IndirectionExceptionTest
 */

public class IndirectionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.portable.IndirectionException ie = new org.omg.CORBA.portable.IndirectionException(5);
        check(ie.offset == 5, "offset stored");
        check(ie instanceof org.omg.CORBA.SystemException, "extends SystemException");
        check(ie.minor == 0, "minor default 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
