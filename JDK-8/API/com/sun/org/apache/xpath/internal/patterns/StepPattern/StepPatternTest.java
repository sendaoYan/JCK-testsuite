/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.patterns.StepPattern conformance: reflection.
 * @run main StepPatternTest
 */

public class StepPatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.patterns.StepPattern", false, StepPatternTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StepPattern"), "simple name StepPattern");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.patterns.NodeTest"), "superclass com.sun.org.apache.xpath.internal.patterns.NodeTest");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.SubContextList"), "interface com.sun.org.apache.xpath.internal.axes.SubContextList");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.ExpressionOwner"), "interface com.sun.org.apache.xpath.internal.ExpressionOwner");
        check(has(c, "calcTargetString"), "method calcTargetString");
        check(has(c, "getTargetString"), "method getTargetString");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "setRelativePathPattern"), "method setRelativePathPattern");
        check(has(c, "getRelativePathPattern"), "method getRelativePathPattern");
        check(has(c, "getPredicates"), "method getPredicates");
        check(has(c, "canTraverseOutsideSubtree"), "method canTraverseOutsideSubtree");
        check(has(c, "getPredicate"), "method getPredicate");
        check(has(c, "getPredicateCount"), "method getPredicateCount");
        check(has(c, "setPredicates"), "method setPredicates");
        check(has(c, "calcScore"), "method calcScore");
        check(has(c, "execute"), "method execute");
        check(has(c, "getProximityPosition"), "method getProximityPosition");
        check(has(c, "getLastPos"), "method getLastPos");
        check(has(c, "toString"), "method toString");
        check(has(c, "getMatchScore"), "method getMatchScore");
        check(has(c, "setAxis"), "method setAxis");
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
