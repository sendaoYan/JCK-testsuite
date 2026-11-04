/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.WalkingIterator conformance: reflection.
 * @run main WalkingIteratorTest
 */

public class WalkingIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.WalkingIterator", false, WalkingIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("WalkingIterator"), "simple name WalkingIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.LocPathIterator"), "superclass com.sun.org.apache.xpath.internal.axes.LocPathIterator");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.ExpressionOwner"), "interface com.sun.org.apache.xpath.internal.ExpressionOwner");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "clone"), "method clone");
        check(has(c, "reset"), "method reset");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "getFirstWalker"), "method getFirstWalker");
        check(has(c, "setFirstWalker"), "method setFirstWalker");
        check(has(c, "setLastUsedWalker"), "method setLastUsedWalker");
        check(has(c, "getLastUsedWalker"), "method getLastUsedWalker");
        check(has(c, "detach"), "method detach");
        check(has(c, "fixupVariables"), "method fixupVariables");
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
