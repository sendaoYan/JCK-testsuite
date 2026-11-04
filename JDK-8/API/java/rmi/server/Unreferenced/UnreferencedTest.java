/*
 * @test
 * @summary java.rmi.server.Unreferenced conformance: interface reflection.
 * @run main UnreferencedTest
 */

public class UnreferencedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.Unreferenced");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "unreferenced"), "declares unreferenced");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
