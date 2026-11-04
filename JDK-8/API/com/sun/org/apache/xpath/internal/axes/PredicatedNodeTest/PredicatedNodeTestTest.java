/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest conformance: reflection.
 * @run main PredicatedNodeTestTest
 */

public class PredicatedNodeTestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest", false, PredicatedNodeTestTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("PredicatedNodeTest"), "simple name PredicatedNodeTest");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.patterns.NodeTest"), "superclass com.sun.org.apache.xpath.internal.patterns.NodeTest");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.SubContextList"), "interface com.sun.org.apache.xpath.internal.axes.SubContextList");
        check(has(c, "clone"), "method clone");
        check(has(c, "getPredicateCount"), "method getPredicateCount");
        check(has(c, "setPredicateCount"), "method setPredicateCount");
        check(has(c, "getPredicate"), "method getPredicate");
        check(has(c, "getProximityPosition"), "method getProximityPosition");
        check(has(c, "getLastPos"), "method getLastPos");
        check(has(c, "resetProximityPositions"), "method resetProximityPositions");
        check(has(c, "initProximityPosition"), "method initProximityPosition");
        check(has(c, "isReverseAxes"), "method isReverseAxes");
        check(has(c, "getPredicateIndex"), "method getPredicateIndex");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "acceptNode"), "method acceptNode");
        check(has(c, "getLocPathIterator"), "method getLocPathIterator");
        check(has(c, "setLocPathIterator"), "method setLocPathIterator");
        check(has(c, "canTraverseOutsideSubtree"), "method canTraverseOutsideSubtree");
        check(has(c, "callPredicateVisitors"), "method callPredicateVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
