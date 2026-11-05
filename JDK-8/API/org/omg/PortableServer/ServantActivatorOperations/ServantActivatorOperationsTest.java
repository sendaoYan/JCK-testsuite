/*
 * @test
 * @summary org.omg.PortableServer.ServantActivatorOperations conformance: interface shape.
 * @run main ServantActivatorOperationsTest
 */

public class ServantActivatorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantActivatorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantManagerOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManagerOperations");
        // ---- deepened (jdk8u372-b07 probed): RETAIN callback face ----
        java.lang.reflect.Method[] ops = c.getDeclaredMethods();
        check(ops.length == 2, "exactly 2 methods: incarnate/etherealize");
        check(c.getInterfaces().length == 1
           && c.getInterfaces()[0] == org.omg.PortableServer.ServantManagerOperations.class,
              "parent is ServantManagerOperations only");
        java.lang.reflect.Method inc = c.getDeclaredMethod("incarnate", byte[].class, org.omg.PortableServer.POA.class);
        check(inc.getReturnType() == org.omg.PortableServer.Servant.class, "incarnate(byte[], POA) -> Servant");
        Class<?>[] exc = inc.getExceptionTypes();
        check(exc.length == 1 && exc[0] == org.omg.PortableServer.ForwardRequest.class,
              "incarnate throws exactly ForwardRequest");
        java.lang.reflect.Method eth = c.getDeclaredMethod("etherealize", byte[].class,
              org.omg.PortableServer.POA.class, org.omg.PortableServer.Servant.class, boolean.class, boolean.class);
        check(eth.getReturnType() == void.class && eth.getExceptionTypes().length == 0,
              "etherealize(byte[], POA, Servant, boolean, boolean) -> void, no checked exceptions");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
