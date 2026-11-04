/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.FilterExprWalker conformance: reflection.
 * @run main FilterExprWalkerTest
 */

public class FilterExprWalkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.FilterExprWalker", false, FilterExprWalkerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FilterExprWalker"), "simple name FilterExprWalker");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.AxesWalker"), "superclass com.sun.org.apache.xpath.internal.axes.AxesWalker");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "init"), "method init");
        check(has(c, "detach"), "method detach");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "clone"), "method clone");
        check(has(c, "acceptNode"), "method acceptNode");
        check(has(c, "getNextNode"), "method getNextNode");
        check(has(c, "getLastPos"), "method getLastPos");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "getInnerExpression"), "method getInnerExpression");
        check(has(c, "setInnerExpression"), "method setInnerExpression");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        check(has(c, "getAxis"), "method getAxis");
        check(has(c, "callPredicateVisitors"), "method callPredicateVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
