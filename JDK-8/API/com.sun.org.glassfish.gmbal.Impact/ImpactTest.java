/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.Impact conformance: reflection.
 * @run main ImpactTest
 */

public class ImpactTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.Impact", false, ImpactTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Impact"), "simple name Impact");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "values"), "method values");
        check(has(c, "valueOf"), "method valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
