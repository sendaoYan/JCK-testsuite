/*
 * @test
 * @summary org.omg.PortableInterceptor.CurrentOperations conformance: interface shape.
 * @run main CurrentOperationsTest
 */

public class CurrentOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.CurrentOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.CurrentOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.CurrentOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
