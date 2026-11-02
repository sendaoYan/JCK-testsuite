/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.Attributes conformance: reflection.
 * @run main AttributesTest
 */

public class AttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.Attributes", false, AttributesTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Attributes"), "simple name Attributes");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.AttributeList"), "interface org.xml.sax.AttributeList");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "getName"), "method getName");
        check(has(c, "getType"), "method getType");
        check(has(c, "getValue"), "method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
