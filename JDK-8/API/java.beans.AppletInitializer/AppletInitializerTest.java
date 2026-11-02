/*
 * @test
 * @summary java.beans.AppletInitializer conformance: interface reflection.
 * @run main AppletInitializerTest
 */

public class AppletInitializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.AppletInitializer");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "activate"), "declares activate");
        check(hasMethod(c, "initialize"), "declares initialize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
