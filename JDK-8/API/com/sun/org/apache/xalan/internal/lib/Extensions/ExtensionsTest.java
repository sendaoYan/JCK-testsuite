/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.Extensions conformance: reflection.
 * @run main ExtensionsTest
 */

public class ExtensionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.Extensions", false, ExtensionsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Extensions"), "simple name Extensions");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "nodeset"), "method nodeset");
        check(has(c, "intersection"), "method intersection");
        check(has(c, "difference"), "method difference");
        check(has(c, "distinct"), "method distinct");
        check(has(c, "hasSameNodes"), "method hasSameNodes");
        check(has(c, "evaluate"), "method evaluate");
        check(has(c, "tokenize"), "method tokenize");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
