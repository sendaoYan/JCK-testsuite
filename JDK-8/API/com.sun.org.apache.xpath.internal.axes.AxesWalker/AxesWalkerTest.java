/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.AxesWalker conformance: reflection.
 * @run main AxesWalkerTest
 */

public class AxesWalkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.AxesWalker", false, AxesWalkerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AxesWalker"), "simple name AxesWalker");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest"), "superclass com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.PathComponent"), "interface com.sun.org.apache.xpath.internal.axes.PathComponent");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.ExpressionOwner"), "interface com.sun.org.apache.xpath.internal.ExpressionOwner");
        check(has(c, "wi"), "method wi");
        check(has(c, "init"), "method init");
        check(has(c, "clone"), "method clone");
        check(has(c, "detach"), "method detach");
        check(has(c, "getRoot"), "method getRoot");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "setNextWalker"), "method setNextWalker");
        check(has(c, "getNextWalker"), "method getNextWalker");
        check(has(c, "setPrevWalker"), "method setPrevWalker");
        check(has(c, "getPrevWalker"), "method getPrevWalker");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "getLastPos"), "method getLastPos");
        check(has(c, "setDefaultDTM"), "method setDefaultDTM");
        check(has(c, "getDTM"), "method getDTM");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        check(has(c, "getAxis"), "method getAxis");
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
