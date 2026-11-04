/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.XPath conformance: reflection.
 * @run main XPathTest
 */

public class XPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.XPath", false, XPathTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPath"), "simple name XPath");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.ExpressionOwner"), "interface com.sun.org.apache.xpath.internal.ExpressionOwner");
        check(has(c, "getExpression"), "method getExpression");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "setExpression"), "method setExpression");
        check(has(c, "getLocator"), "method getLocator");
        check(has(c, "getPatternString"), "method getPatternString");
        check(has(c, "execute"), "method execute");
        check(has(c, "bool"), "method bool");
        check(has(c, "getMatchScore"), "method getMatchScore");
        check(has(c, "warn"), "method warn");
        check(has(c, "assertion"), "method assertion");
        check(has(c, "error"), "method error");
        check(has(c, "callVisitors"), "method callVisitors");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
