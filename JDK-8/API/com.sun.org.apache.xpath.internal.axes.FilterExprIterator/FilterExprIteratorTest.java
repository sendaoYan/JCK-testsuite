/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.FilterExprIterator conformance: reflection.
 * @run main FilterExprIteratorTest
 */

public class FilterExprIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.FilterExprIterator", false, FilterExprIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FilterExprIterator"), "simple name FilterExprIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.BasicTestIterator"), "superclass com.sun.org.apache.xpath.internal.axes.BasicTestIterator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "detach"), "method detach");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "getInnerExpression"), "method getInnerExpression");
        check(has(c, "setInnerExpression"), "method setInnerExpression");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        check(has(c, "callPredicateVisitors"), "method callPredicateVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
