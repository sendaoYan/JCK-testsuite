/*
 * @test
 * @summary javax.management.loading.PrivateMLet conformance: concrete class reflection.
 * @run main PrivateMLetTest
 */

public class PrivateMLetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.PrivateMLet");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.loading.MLet".equals(c.getSuperclass().getName()), "superclass javax.management.loading.MLet");
        check(Class.forName("javax.management.loading.PrivateClassLoader").isAssignableFrom(c), "implements/extends javax.management.loading.PrivateClassLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
