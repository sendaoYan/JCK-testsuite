/*
 * @test
 * @summary org.omg.PortableServer.AdapterActivatorOperations conformance: interface shape.
 * @run main AdapterActivatorOperationsTest
 */

public class AdapterActivatorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.AdapterActivatorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): single-callback face ----
        java.lang.reflect.Method[] ops = c.getDeclaredMethods();
        check(ops.length == 1, "exactly one method: unknown_adapter");
        java.lang.reflect.Method u = c.getDeclaredMethod("unknown_adapter",
              org.omg.PortableServer.POA.class, String.class);
        check(u.getReturnType() == boolean.class, "unknown_adapter(POA, String) -> boolean");
        check(c.getInterfaces().length == 0, "no parent interface");
        check(u.getExceptionTypes().length == 0, "declares no checked exceptions");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
