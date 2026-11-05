/*
 * @test
 * @summary org.omg.PortableServer.AdapterActivator conformance: interface shape.
 * @run main AdapterActivatorTest
 */

public class AdapterActivatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.AdapterActivator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.AdapterActivatorOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.AdapterActivatorOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): no own methods / extends set / proxy callback ----
        check(c.getDeclaredMethods().length == 0, "AdapterActivator declares no own methods");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.AdapterActivatorOperations.class
           && ext[1] == org.omg.CORBA.Object.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { AdapterActivatorOperations, CORBA.Object, IDLEntity } in order");
        final boolean[] routed = new boolean[1];
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            if ("unknown_adapter".equals(m.getName())) { routed[0] = true; return Boolean.TRUE; }
            return null;
        };
        org.omg.PortableServer.AdapterActivator aa = (org.omg.PortableServer.AdapterActivator)
            java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[]{c}, ih);
        check(aa.unknown_adapter(null, "child") && routed[0],
              "proxy unknown_adapter(parent, name) routes true (POA-creates-child callback channel)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
