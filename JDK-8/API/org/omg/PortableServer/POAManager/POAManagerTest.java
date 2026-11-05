/*
 * @test
 * @summary org.omg.PortableServer.POAManager conformance: interface shape.
 * @run main POAManagerTest
 */

public class POAManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.POAManager");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.POAManagerOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.POAManagerOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): no own methods / extends set / proxy get_state routing ----
        check(c.getDeclaredMethods().length == 0, "POAManager declares no own methods");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.POAManagerOperations.class
           && ext[1] == org.omg.CORBA.Object.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { POAManagerOperations, CORBA.Object, IDLEntity } in order");
        final boolean[] routed = new boolean[1];
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            if ("get_state".equals(m.getName())) { routed[0] = true; return org.omg.PortableServer.POAManagerPackage.State.ACTIVE; }
            return null;
        };
        org.omg.PortableServer.POAManager pm = (org.omg.PortableServer.POAManager) java.lang.reflect.Proxy.newProxyInstance(
            c.getClassLoader(), new Class<?>[]{c}, ih);
        check(pm.get_state() == org.omg.PortableServer.POAManagerPackage.State.ACTIVE && routed[0],
              "proxy get_state() routes State.ACTIVE");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
