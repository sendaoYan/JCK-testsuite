/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.XPathContext.XPathExpressionContext conformance: reflection.
 * @run main XPathExpressionContextTest
 */

public class XPathExpressionContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.XPathContext$XPathExpressionContext", false, XPathExpressionContextTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathExpressionContext"), "simple name XPathExpressionContext");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.extensions.ExpressionContext"), "interface com.sun.org.apache.xalan.internal.extensions.ExpressionContext");
        check(has(c, "getXPathContext"), "method getXPathContext");
        check(has(c, "getDTMManager"), "method getDTMManager");
        check(has(c, "getContextNode"), "method getContextNode");
        check(has(c, "getContextNodes"), "method getContextNodes");
        check(has(c, "getErrorListener"), "method getErrorListener");
        check(has(c, "overrideDefaultParser"), "method overrideDefaultParser");
        check(has(c, "setOverrideDefaultParser"), "method setOverrideDefaultParser");
        check(has(c, "toNumber"), "method toNumber");
        check(has(c, "toString"), "method toString");
        check(has(c, "getVariableOrParam"), "method getVariableOrParam");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
