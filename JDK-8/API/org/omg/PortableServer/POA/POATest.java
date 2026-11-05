/*
 * @test
 * @summary org.omg.PortableServer.POA conformance: interface shape.
 * @run main POATest
 */

public class POATest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.POA");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.POAOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.POAOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): no own methods / exact extends / proxy routing ----
        check(c.getDeclaredMethods().length == 0, "POA interface declares no own methods (all on POAOperations)");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.POAOperations.class
           && ext[1] == org.omg.CORBA.Object.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { POAOperations, CORBA.Object, IDLEntity } in order");
        check(org.omg.PortableServer.POAOperations.class.getDeclaredMethods().length == 32,
              "POAOperations method face = 32 methods");
        final boolean[] routed = new boolean[3];
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            if ("the_name".equals(m.getName())) { routed[0] = true; return "rootPOA"; }
            if ("id".equals(m.getName())) { routed[1] = true; return new byte[]{1, 2}; }
            if ("the_children".equals(m.getName())) { routed[2] = true; return new org.omg.PortableServer.POA[0]; }
            return null;
        };
        org.omg.PortableServer.POA poa = (org.omg.PortableServer.POA) java.lang.reflect.Proxy.newProxyInstance(
            c.getClassLoader(), new Class<?>[]{c}, ih);
        check(poa.the_name().equals("rootPOA") && routed[0], "proxy the_name() routes");
        check(java.util.Arrays.equals(poa.id(), new byte[]{1, 2}) && routed[1], "proxy id() routes");
        check(poa.the_children() != null && poa.the_children().length == 0 && routed[2], "proxy the_children() routes");
        check(poa instanceof org.omg.CORBA.Object, "POA reference is a CORBA Object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
