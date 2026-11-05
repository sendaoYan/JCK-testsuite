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
        // ---- deepened (jdk8u372-b07 probed): NON_RETAIN callback face ----
        java.lang.reflect.Method[] ops = c.getDeclaredMethods();
        check(ops.length == 2, "exactly 2 methods: preinvoke/postinvoke");
        check(c.getInterfaces().length == 1
           && c.getInterfaces()[0] == org.omg.PortableServer.ServantManagerOperations.class,
              "parent is ServantManagerOperations only");
        java.lang.reflect.Method pre = c.getDeclaredMethod("preinvoke", byte[].class,
              org.omg.PortableServer.POA.class, String.class,
              org.omg.PortableServer.ServantLocatorPackage.CookieHolder.class);
        check(pre.getReturnType() == org.omg.PortableServer.Servant.class,
              "preinvoke(byte[], POA, String, CookieHolder) -> Servant");
        Class<?>[] exc = pre.getExceptionTypes();
        check(exc.length == 1 && exc[0] == org.omg.PortableServer.ForwardRequest.class,
              "preinvoke throws exactly ForwardRequest");
        java.lang.reflect.Method post = c.getDeclaredMethod("postinvoke", byte[].class,
              org.omg.PortableServer.POA.class, String.class, java.lang.Object.class,
              org.omg.PortableServer.Servant.class);
        check(post.getReturnType() == void.class && post.getExceptionTypes().length == 0,
              "postinvoke(byte[], POA, String, Object cookie, Servant) -> void, no checked exceptions");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
