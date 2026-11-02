/*
 * @test
 * @summary org.omg.PortableServer.ServantLocatorOperations conformance: interface shape.
 * @run main ServantLocatorOperationsTest
 */

public class ServantLocatorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantLocatorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantManagerOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManagerOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
