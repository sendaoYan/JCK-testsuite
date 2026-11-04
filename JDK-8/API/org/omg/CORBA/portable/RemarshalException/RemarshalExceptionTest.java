/*
 * @test
 * @summary org.omg.CORBA.portable.RemarshalException conformance: exception hierarchy.
 * @run main RemarshalExceptionTest
 */

public class RemarshalExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.portable.RemarshalException r = new org.omg.CORBA.portable.RemarshalException();
        check(r instanceof java.lang.Exception, "extends Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
