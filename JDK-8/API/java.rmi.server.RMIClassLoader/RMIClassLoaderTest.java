/*
 * @test
 * @summary java.rmi.server.RMIClassLoader conformance: concrete class reflection.
 * @run main RMIClassLoaderTest
 */

public class RMIClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RMIClassLoader");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "getClassAnnotation"), "declares getClassAnnotation");
        check(hasMethod(c, "getClassLoader"), "declares getClassLoader");
        check(hasMethod(c, "getDefaultProviderInstance"), "declares getDefaultProviderInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
