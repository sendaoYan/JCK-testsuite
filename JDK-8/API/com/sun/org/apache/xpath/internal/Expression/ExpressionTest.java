/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.Expression conformance: reflection.
 * @run main ExpressionTest
 */

public class ExpressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.Expression", false, ExpressionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Expression"), "simple name Expression");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.ExpressionNode"), "interface com.sun.org.apache.xpath.internal.ExpressionNode");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.XPathVisitable"), "interface com.sun.org.apache.xpath.internal.XPathVisitable");
        check(has(c, "canTraverseOutsideSubtree"), "method canTraverseOutsideSubtree");
        check(has(c, "execute"), "method execute");
        check(has(c, "num"), "method num");
        check(has(c, "bool"), "method bool");
        check(has(c, "xstr"), "method xstr");
        check(has(c, "isNodesetExpr"), "method isNodesetExpr");
        check(has(c, "asNode"), "method asNode");
        check(has(c, "asIterator"), "method asIterator");
        check(has(c, "asIteratorRaw"), "method asIteratorRaw");
        check(has(c, "executeCharsToContentHandler"), "method executeCharsToContentHandler");
        check(has(c, "isStableNumber"), "method isStableNumber");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "deepEquals"), "method deepEquals");
        check(has(c, "warn"), "method warn");
        check(has(c, "assertion"), "method assertion");
        check(has(c, "error"), "method error");
        check(has(c, "getExpressionOwner"), "method getExpressionOwner");
        check(has(c, "exprSetParent"), "method exprSetParent");
        check(has(c, "exprGetParent"), "method exprGetParent");
        check(has(c, "exprAddChild"), "method exprAddChild");
        check(has(c, "exprGetChild"), "method exprGetChild");
        check(has(c, "exprGetNumChildren"), "method exprGetNumChildren");
        check(has(c, "getPublicId"), "method getPublicId");
        check(has(c, "getSystemId"), "method getSystemId");
        check(has(c, "getLineNumber"), "method getLineNumber");
        check(has(c, "getColumnNumber"), "method getColumnNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
