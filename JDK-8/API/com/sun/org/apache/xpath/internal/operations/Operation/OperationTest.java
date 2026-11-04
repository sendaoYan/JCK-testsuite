/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.operations.Operation conformance: reflection.
 * @run main OperationTest
 */

public class OperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.operations.Operation", false, OperationTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Operation"), "simple name Operation");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.Expression"), "superclass com.sun.org.apache.xpath.internal.Expression");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.ExpressionOwner"), "interface com.sun.org.apache.xpath.internal.ExpressionOwner");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "canTraverseOutsideSubtree"), "method canTraverseOutsideSubtree");
        check(has(c, "setLeftRight"), "method setLeftRight");
        check(has(c, "execute"), "method execute");
        check(has(c, "operate"), "method operate");
        check(has(c, "getLeftOperand"), "method getLeftOperand");
        check(has(c, "getRightOperand"), "method getRightOperand");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "getExpression"), "method getExpression");
        check(has(c, "setExpression"), "method setExpression");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
