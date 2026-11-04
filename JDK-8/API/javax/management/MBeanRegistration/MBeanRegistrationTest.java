/*
 * @test
 * @summary javax.management.MBeanRegistration conformance: interface reflection.
 * @run main MBeanRegistrationTest
 */

public class MBeanRegistrationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanRegistration");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "postDeregister"), "declares postDeregister");
        check(hasMethod(c, "postRegister"), "declares postRegister");
        check(hasMethod(c, "preDeregister"), "declares preDeregister");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
