/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltStrings conformance: reflection.
 * @run main ExsltStringsTest
 */

public class ExsltStringsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltStrings", false, ExsltStringsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExsltStrings"), "simple name ExsltStrings");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltBase"), "superclass com.sun.org.apache.xalan.internal.lib.ExsltBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "align"), "method align");
        check(has(c, "concat"), "method concat");
        check(has(c, "padding"), "method padding");
        check(has(c, "split"), "method split");
        check(has(c, "tokenize"), "method tokenize");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
