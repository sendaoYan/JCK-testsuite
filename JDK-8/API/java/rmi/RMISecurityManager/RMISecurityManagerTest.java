/*
 * @test
 * @summary java.rmi.RMISecurityManager conformance: concrete class reflection.
 * @run main RMISecurityManagerTest
 */

public class RMISecurityManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.RMISecurityManager");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.lang.SecurityManager".equals(c.getSuperclass().getName()), "superclass java.lang.SecurityManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
