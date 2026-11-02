/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex.KeyIndexIterator.KeyIndexHeapNode conformance: reflection.
 * @run main KeyIndexHeapNodeTest
 */

public class KeyIndexHeapNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex$KeyIndexIterator$KeyIndexHeapNode", false, KeyIndexHeapNodeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("KeyIndexHeapNode"), "simple name KeyIndexHeapNode");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator$HeapNode"), "superclass com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator$HeapNode");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "step"), "method step");
        check(has(c, "cloneHeapNode"), "method cloneHeapNode");
        check(has(c, "setMark"), "method setMark");
        check(has(c, "gotoMark"), "method gotoMark");
        check(has(c, "isLessThan"), "method isLessThan");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
