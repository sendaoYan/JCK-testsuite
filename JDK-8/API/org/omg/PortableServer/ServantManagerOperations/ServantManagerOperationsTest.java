/*
 * @test
 * @summary org.omg.PortableServer.ServantManagerOperations conformance: interface shape.
 * @run main ServantManagerOperationsTest
 */

public class ServantManagerOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantManagerOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): empty marker face ----
        check(c.getDeclaredMethods().length == 0, "ServantManagerOperations declares no methods");
        check(c.getInterfaces().length == 0, "no parent interfaces");
        check(c.getMethods().length == 0, "inherited face is empty too (pure marker)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
