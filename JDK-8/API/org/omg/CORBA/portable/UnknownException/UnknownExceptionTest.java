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

        // ---- deepened: defaults, toString, finality ----
        check(ue.originalEx == t, "originalEx stays public-readable");
        check(ue.toString().contains("UnknownException"), "toString names the class");
        org.omg.CORBA.portable.UnknownException ue2 =
            new org.omg.CORBA.portable.UnknownException(new RuntimeException("r"));
        check("r".equals(ue2.originalEx.getMessage()), "second instance original readback");
        check(ue2.minor == 0, "minor default 0");
        check(!java.lang.reflect.Modifier.isFinal(org.omg.CORBA.portable.UnknownException.class.getModifiers()),
              "UnknownException is NOT final (jdk8u372)");
        check(org.omg.CORBA.portable.UnknownException.class.getConstructors().length == 1,
              "single (Throwable) constructor");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
