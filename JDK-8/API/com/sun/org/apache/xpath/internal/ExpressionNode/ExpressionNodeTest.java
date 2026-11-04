/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.ExpressionNode conformance: reflection.
 * @run main ExpressionNodeTest
 */

public class ExpressionNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.ExpressionNode", false, ExpressionNodeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExpressionNode"), "simple name ExpressionNode");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.xml.transform.SourceLocator"), "interface javax.xml.transform.SourceLocator");
        check(has(c, "exprSetParent"), "method exprSetParent");
        check(has(c, "exprGetParent"), "method exprGetParent");
        check(has(c, "exprAddChild"), "method exprAddChild");
        check(has(c, "exprGetChild"), "method exprGetChild");
        check(has(c, "exprGetNumChildren"), "method exprGetNumChildren");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
