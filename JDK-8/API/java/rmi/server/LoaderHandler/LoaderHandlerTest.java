/*
 * @test
 * @summary java.rmi.server.LoaderHandler conformance: interface reflection.
 * @run main LoaderHandlerTest
 */

public class LoaderHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.LoaderHandler");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getSecurityContext"), "declares getSecurityContext");
        check(hasMethod(c, "loadClass"), "declares loadClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
