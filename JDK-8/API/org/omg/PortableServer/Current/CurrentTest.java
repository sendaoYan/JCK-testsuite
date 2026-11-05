/*
 * @test
 * @summary org.omg.PortableServer.Current conformance: interface shape.
 * @run main CurrentTest
 */

public class CurrentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.Current");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.CurrentOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.CurrentOperations");
        check(org.omg.CORBA.Current.class.isAssignableFrom(c), "extends org.omg.CORBA.Current");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): no own methods / extends set / proxy routing ----
        check(c.getDeclaredMethods().length == 0, "Current declares no own methods");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.CurrentOperations.class
           && ext[1] == org.omg.CORBA.Current.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { CurrentOperations, CORBA.Current, IDLEntity } in order");
        Class<?>[] corbaCur = org.omg.CORBA.Current.class.getInterfaces();
        check(corbaCur.length == 3
           && corbaCur[0] == org.omg.CORBA.CurrentOperations.class
           && corbaCur[1] == org.omg.CORBA.Object.class
           && corbaCur[2] == org.omg.CORBA.portable.IDLEntity.class,
              "CORBA.Current extends { CurrentOperations, Object, IDLEntity } in order");
        final boolean[] routed = new boolean[2];
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            if ("get_POA".equals(m.getName())) { routed[0] = true; return null; }
            if ("get_object_id".equals(m.getName())) { routed[1] = true; return new byte[]{9}; }
            return null;
        };
        org.omg.PortableServer.Current cur = (org.omg.PortableServer.Current) java.lang.reflect.Proxy.newProxyInstance(
            c.getClassLoader(), new Class<?>[]{c}, ih);
        cur.get_POA();
        check(routed[0], "proxy get_POA() routes");
        check(java.util.Arrays.equals(cur.get_object_id(), new byte[]{9}) && routed[1], "proxy get_object_id() routes");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
