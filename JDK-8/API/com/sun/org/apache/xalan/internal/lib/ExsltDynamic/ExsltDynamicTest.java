/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltDynamic conformance: reflection.
 * @run main ExsltDynamicTest
 */

public class ExsltDynamicTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltDynamic", false, ExsltDynamicTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExsltDynamic"), "simple name ExsltDynamic");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltBase"), "superclass com.sun.org.apache.xalan.internal.lib.ExsltBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "max"), "method max");
        check(has(c, "min"), "method min");
        check(has(c, "sum"), "method sum");
        check(has(c, "map"), "method map");
        check(has(c, "evaluate"), "method evaluate");
        check(has(c, "closure"), "method closure");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
