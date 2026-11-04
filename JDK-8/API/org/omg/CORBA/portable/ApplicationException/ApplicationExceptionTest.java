/*
 * @test
 * @summary org.omg.CORBA.portable.ApplicationException conformance: concrete class behavior.
 * @run main ApplicationExceptionTest
 */

public class ApplicationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.portable.ApplicationException ae = new org.omg.CORBA.portable.ApplicationException("IDL:test:1.0", null);
        check("IDL:test:1.0".equals(ae.getId()), "getId");
        check(ae.getInputStream() == null, "getInputStream null");
        check(ae instanceof java.lang.Exception, "extends Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
