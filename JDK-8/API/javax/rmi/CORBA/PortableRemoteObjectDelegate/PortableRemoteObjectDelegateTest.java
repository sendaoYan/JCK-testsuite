/*
 * @test
 * @summary javax.rmi.CORBA.PortableRemoteObjectDelegate conformance: interface reflection.
 * @run main PortableRemoteObjectDelegateTest
 */

public class PortableRemoteObjectDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.PortableRemoteObjectDelegate");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "narrow"), "declares narrow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
