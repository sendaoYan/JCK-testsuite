/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode conformance: reflection.
 * @run main SyntaxTreeNodeTest
 */

public class SyntaxTreeNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode", false, SyntaxTreeNodeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SyntaxTreeNode"), "simple name SyntaxTreeNode");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants"), "interface com.sun.org.apache.xalan.internal.xsltc.compiler.Constants");
        check(has(c, "getLineNumber"), "method getLineNumber");
        check(has(c, "getParser"), "method getParser");
        check(has(c, "getStylesheet"), "method getStylesheet");
        check(has(c, "parseContents"), "method parseContents");
        check(has(c, "typeCheck"), "method typeCheck");
        check(has(c, "translate"), "method translate");
        check(has(c, "display"), "method display");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
