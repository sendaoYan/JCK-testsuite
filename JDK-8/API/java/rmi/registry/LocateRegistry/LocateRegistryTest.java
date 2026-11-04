/*
 * @test
 * @summary java.rmi.registry.LocateRegistry conformance: concrete class reflection.
 * @run main LocateRegistryTest
 */

public class LocateRegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.registry.LocateRegistry");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "createRegistry"), "declares createRegistry");
        check(hasMethod(c, "getRegistry"), "declares getRegistry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
