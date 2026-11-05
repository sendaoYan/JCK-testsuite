/*
 * @test
 * @summary org.omg.PortableServer.ServantActivator conformance: interface shape.
 * @run main ServantActivatorTest
 */

public class ServantActivatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantActivator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantActivatorOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantActivatorOperations");
        check(org.omg.PortableServer.ServantManager.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManager");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): extends chain exact ----
        check(c.getDeclaredMethods().length == 0, "ServantActivator declares no own methods");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.ServantActivatorOperations.class
           && ext[1] == org.omg.PortableServer.ServantManager.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { ServantActivatorOperations, ServantManager, IDLEntity } in order");
        check(org.omg.PortableServer.ServantManager.class.isAssignableFrom(c),
              "RETAIN servant manager is-a ServantManager");
        final java.util.List<String> calls = new java.util.ArrayList<>();
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            calls.add(m.getName());
            return null;
        };
        org.omg.PortableServer.ServantActivator sa = (org.omg.PortableServer.ServantActivator)
            java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[]{c}, ih);
        sa.incarnate(new byte[]{1}, null);
        sa.etherealize(new byte[]{1}, null, null, true, false);
        check(calls.equals(java.util.Arrays.asList("incarnate", "etherealize")),
              "proxy routes incarnate/etherealize in call order");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
