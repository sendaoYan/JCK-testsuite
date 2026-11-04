/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl conformance: reflection.
 * @run main XPathEvaluatorImplTest
 */

public class XPathEvaluatorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl", false, XPathEvaluatorImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathEvaluatorImpl"), "simple name XPathEvaluatorImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.w3c.dom.xpath.XPathEvaluator"), "interface org.w3c.dom.xpath.XPathEvaluator");
        check(has(c, "createExpression"), "method createExpression");
        check(has(c, "createNSResolver"), "method createNSResolver");
        check(has(c, "evaluate"), "method evaluate");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
