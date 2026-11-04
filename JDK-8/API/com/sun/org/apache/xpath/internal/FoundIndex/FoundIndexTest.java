/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.FoundIndex conformance: reflection.
 * @run main FoundIndexTest
 */

public class FoundIndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.FoundIndex", false, FoundIndexTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FoundIndex"), "simple name FoundIndex");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
