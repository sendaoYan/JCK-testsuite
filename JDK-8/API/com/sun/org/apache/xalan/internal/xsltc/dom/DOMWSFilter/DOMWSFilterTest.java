/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter conformance: reflection.
 * @run main DOMWSFilterTest
 */

public class DOMWSFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter", false, DOMWSFilterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("DOMWSFilter"), "simple name DOMWSFilter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTMWSFilter"), "interface com.sun.org.apache.xml.internal.dtm.DTMWSFilter");
        check(has(c, "getShouldStripSpace"), "method getShouldStripSpace");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
