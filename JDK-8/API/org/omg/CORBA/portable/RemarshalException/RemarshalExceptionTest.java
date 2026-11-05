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

        // ---- deepened: shape ----
        check(!org.omg.CORBA.UserException.class.isAssignableFrom(r.getClass()),
              "plain checked Exception (not a CORBA UserException)");
        check(r.getMessage() == null, "no message slot");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.portable.RemarshalException.class.getModifiers()),
              "RemarshalException is final");
        check(org.omg.CORBA.portable.RemarshalException.class.getConstructors().length == 1,
              "only the no-arg constructor");
        check(r.toString().contains("RemarshalException"), "toString names the class");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
