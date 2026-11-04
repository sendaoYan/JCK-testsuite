/*
 * @test
 * @summary org.omg.PortableServer.IdUniquenessPolicyOperations conformance: interface shape.
 * @run main IdUniquenessPolicyOperationsTest
 */

public class IdUniquenessPolicyOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.IdUniquenessPolicyOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.PolicyOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.PolicyOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
