/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.XPathVisitor conformance: reflection.
 * @run main XPathVisitorTest
 */

public class XPathVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.XPathVisitor", false, XPathVisitorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathVisitor"), "simple name XPathVisitor");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "visitLocationPath"), "method visitLocationPath");
        check(has(c, "visitUnionPath"), "method visitUnionPath");
        check(has(c, "visitStep"), "method visitStep");
        check(has(c, "visitPredicate"), "method visitPredicate");
        check(has(c, "visitBinaryOperation"), "method visitBinaryOperation");
        check(has(c, "visitUnaryOperation"), "method visitUnaryOperation");
        check(has(c, "visitVariableRef"), "method visitVariableRef");
        check(has(c, "visitFunction"), "method visitFunction");
        check(has(c, "visitMatchPattern"), "method visitMatchPattern");
        check(has(c, "visitUnionPattern"), "method visitUnionPattern");
        check(has(c, "visitStringLiteral"), "method visitStringLiteral");
        check(has(c, "visitNumberLiteral"), "method visitNumberLiteral");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
