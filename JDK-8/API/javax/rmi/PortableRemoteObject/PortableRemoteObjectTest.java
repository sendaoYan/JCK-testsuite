/*
 * @test
 * @summary javax.rmi.PortableRemoteObject conformance: concrete class reflection.
 * @run main PortableRemoteObjectTest
 */

public class PortableRemoteObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.PortableRemoteObject");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "narrow"), "declares narrow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
