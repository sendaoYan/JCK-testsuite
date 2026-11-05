/*
 * @test
 * @summary org.omg.PortableServer.ServantLocator conformance: interface shape.
 * @run main ServantLocatorTest
 */

public class ServantLocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantLocatorOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantLocatorOperations");
        check(org.omg.PortableServer.ServantManager.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManager");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): extends chain exact ----
        check(c.getDeclaredMethods().length == 0, "ServantLocator declares no own methods");
        Class<?>[] ext = c.getInterfaces();
        check(ext.length == 3
           && ext[0] == org.omg.PortableServer.ServantLocatorOperations.class
           && ext[1] == org.omg.PortableServer.ServantManager.class
           && ext[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { ServantLocatorOperations, ServantManager, IDLEntity } in order");
        final java.util.List<String> calls = new java.util.ArrayList<>();
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            calls.add(m.getName());
            return null;
        };
        org.omg.PortableServer.ServantLocator sl = (org.omg.PortableServer.ServantLocator)
            java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[]{c}, ih);
        sl.preinvoke(new byte[]{1}, null, "op", null);
        sl.postinvoke(new byte[]{1}, null, "op", null, null);
        check(calls.equals(java.util.Arrays.asList("preinvoke", "postinvoke")),
              "proxy routes preinvoke/postinvoke in call order");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
