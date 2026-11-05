/*
 * @test
 * @summary org.omg.CORBA.CurrentOperations conformance: interface shape.
 * @run main CurrentOperationsTest
 */

public class CurrentOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.CurrentOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        check(org.omg.CORBA.CurrentOperations.class.getMethods().length == 0, "CurrentOperations declares no operations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
