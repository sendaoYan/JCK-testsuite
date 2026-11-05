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

        // ---- deepened: defaults and toString (jdk8u372 measured) ----
        check(ie.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "completed defaults to COMPLETED_MAYBE (unique among SystemExceptions)");
        check(ie.offset == 5, "offset stays public-readable");
        org.omg.CORBA.portable.IndirectionException ie2 = new org.omg.CORBA.portable.IndirectionException(4096);
        check(ie2.offset == 4096, "second instance offset");
        check(ie2.toString().contains("completed: Maybe"), "toString completed: Maybe");
        check(ie2.toString().startsWith("org.omg.CORBA.portable.IndirectionException"), "toString class prefix");
        check(!java.lang.reflect.Modifier.isFinal(org.omg.CORBA.portable.IndirectionException.class.getModifiers()),
              "IndirectionException is NOT final (jdk8u372)");
        check(org.omg.CORBA.portable.IndirectionException.class.getConstructors().length == 1,
              "single (int) constructor");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
