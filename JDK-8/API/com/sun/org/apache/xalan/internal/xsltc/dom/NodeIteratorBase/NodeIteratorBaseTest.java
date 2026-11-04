/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.NodeIteratorBase conformance: reflection.
 * @run main NodeIteratorBaseTest
 */

public class NodeIteratorBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.NodeIteratorBase", false, NodeIteratorBaseTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NodeIteratorBase"), "simple name NodeIteratorBase");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.NodeIterator"), "interface com.sun.org.apache.xalan.internal.xsltc.NodeIterator");
        check(has(c, "setRestartable"), "method setRestartable");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "reset"), "method reset");
        check(has(c, "includeSelf"), "method includeSelf");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "isReverse"), "method isReverse");
        check(has(c, "cloneIterator"), "method cloneIterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
