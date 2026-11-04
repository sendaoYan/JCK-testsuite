/*
 * @test
 * @summary javax.rmi.CORBA.Stub conformance: abstract class reflection.
 * @run main StubTest
 */

public class StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.Stub");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("org.omg.CORBA_2_3.portable.ObjectImpl".equals(c.getSuperclass().getName()), "superclass org.omg.CORBA_2_3.portable.ObjectImpl");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
