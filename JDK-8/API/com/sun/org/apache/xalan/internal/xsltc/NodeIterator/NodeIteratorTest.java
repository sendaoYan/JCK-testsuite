/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.NodeIterator conformance: reflection.
 * @run main NodeIteratorTest
 */

public class NodeIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.NodeIterator", false, NodeIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeIterator"), "simple name NodeIterator");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "next"), "method next");
        check(has(c, "reset"), "method reset");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "setMark"), "method setMark");
        check(has(c, "gotoMark"), "method gotoMark");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "isReverse"), "method isReverse");
        check(has(c, "cloneIterator"), "method cloneIterator");
        check(has(c, "setRestartable"), "method setRestartable");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
