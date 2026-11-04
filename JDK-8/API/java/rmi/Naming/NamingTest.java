/*
 * @test
 * @summary java.rmi.Naming conformance: concrete class reflection.
 * @run main NamingTest
 */

public class NamingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.Naming");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "bind"), "declares bind");
        check(hasMethod(c, "list"), "declares list");
        check(hasMethod(c, "lookup"), "declares lookup");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
