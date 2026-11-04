/*
 * @test
 * @summary java.rmi.server.LogStream conformance: concrete class reflection.
 * @run main LogStreamTest
 */

public class LogStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.LogStream");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.io.PrintStream".equals(c.getSuperclass().getName()), "superclass java.io.PrintStream");
        check(hasMethod(c, "getDefaultStream"), "declares getDefaultStream");
        check(hasMethod(c, "getOutputStream"), "declares getOutputStream");
        check(hasMethod(c, "log"), "declares log");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
