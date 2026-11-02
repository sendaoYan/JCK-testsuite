/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.jaxp.XPathImpl conformance: reflection.
 * @run main XPathImplTest
 */

public class XPathImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.jaxp.XPathImpl", false, XPathImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathImpl"), "simple name XPathImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.xml.xpath.XPath"), "interface javax.xml.xpath.XPath");
        check(has(c, "setXPathVariableResolver"), "method setXPathVariableResolver");
        check(has(c, "getXPathVariableResolver"), "method getXPathVariableResolver");
        check(has(c, "setXPathFunctionResolver"), "method setXPathFunctionResolver");
        check(has(c, "getXPathFunctionResolver"), "method getXPathFunctionResolver");
        check(has(c, "setNamespaceContext"), "method setNamespaceContext");
        check(has(c, "getNamespaceContext"), "method getNamespaceContext");
        check(has(c, "evaluate"), "method evaluate");
        check(has(c, "compile"), "method compile");
        check(has(c, "reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
