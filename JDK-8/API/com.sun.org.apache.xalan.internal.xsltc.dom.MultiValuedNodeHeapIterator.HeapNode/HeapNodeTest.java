/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode conformance: reflection.
 * @run main HeapNodeTest
 */

public class HeapNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator$HeapNode", false, HeapNodeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("HeapNode"), "simple name HeapNode");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
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
