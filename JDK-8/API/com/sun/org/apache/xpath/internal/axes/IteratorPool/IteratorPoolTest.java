/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.IteratorPool conformance: reflection.
 * @run main IteratorPoolTest
 */

public class IteratorPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.IteratorPool", false, IteratorPoolTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("IteratorPool"), "simple name IteratorPool");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "getInstanceOrThrow"), "method getInstanceOrThrow");
        check(has(c, "getInstance"), "method getInstance");
        check(has(c, "freeInstance"), "method freeInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
