/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltSets conformance: reflection.
 * @run main ExsltSetsTest
 */

public class ExsltSetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltSets", false, ExsltSetsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExsltSets"), "simple name ExsltSets");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltBase"), "superclass com.sun.org.apache.xalan.internal.lib.ExsltBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "leading"), "method leading");
        check(has(c, "trailing"), "method trailing");
        check(has(c, "intersection"), "method intersection");
        check(has(c, "difference"), "method difference");
        check(has(c, "distinct"), "method distinct");
        check(has(c, "hasSameNode"), "method hasSameNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
