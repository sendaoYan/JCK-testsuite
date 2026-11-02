/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.extensions.ExpressionContext conformance: reflection.
 * @run main ExpressionContextTest
 */

public class ExpressionContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.extensions.ExpressionContext", false, ExpressionContextTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExpressionContext"), "simple name ExpressionContext");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getContextNode"), "method getContextNode");
        check(has(c, "getContextNodes"), "method getContextNodes");
        check(has(c, "getErrorListener"), "method getErrorListener");
        check(has(c, "toNumber"), "method toNumber");
        check(has(c, "toString"), "method toString");
        check(has(c, "getVariableOrParam"), "method getVariableOrParam");
        check(has(c, "getXPathContext"), "method getXPathContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
