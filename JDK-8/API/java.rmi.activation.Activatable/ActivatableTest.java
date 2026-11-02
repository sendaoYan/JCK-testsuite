/*
 * @test
 * @summary java.rmi.activation.Activatable conformance: abstract class reflection.
 * @run main ActivatableTest
 */

public class ActivatableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.Activatable");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("java.rmi.server.RemoteServer".equals(c.getSuperclass().getName()), "superclass java.rmi.server.RemoteServer");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "inactive"), "declares inactive");
        check(hasMethod(c, "register"), "declares register");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
