/*
 * @test
 * @summary org.omg.PortableServer.ServantManager conformance: interface shape.
 * @run main ServantManagerTest
 */

public class ServantManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantManager");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantManagerOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManagerOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): marker interface shape ----
        check(c.getDeclaredMethods().length == 0, "ServantManager declares no own methods (marker)");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.ServantManagerOperations.class
           && ext[1] == org.omg.CORBA.Object.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { ServantManagerOperations, CORBA.Object, IDLEntity } in order");
        org.omg.PortableServer.ServantManager sm = (org.omg.PortableServer.ServantManager)
            java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[]{c},
                (px, m, av) -> null);
        check(sm != null && sm instanceof org.omg.CORBA.Object,
              "proxy over the marker interface is usable as a CORBA Object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
