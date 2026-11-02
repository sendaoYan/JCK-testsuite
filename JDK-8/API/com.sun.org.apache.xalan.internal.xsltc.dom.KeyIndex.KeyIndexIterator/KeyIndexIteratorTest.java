/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex.KeyIndexIterator conformance: reflection.
 * @run main KeyIndexIteratorTest
 */

public class KeyIndexIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex$KeyIndexIterator", false, KeyIndexIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("KeyIndexIterator"), "simple name KeyIndexIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator"), "superclass com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "next"), "method next");
        check(has(c, "reset"), "method reset");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "getNodeByPosition"), "method getNodeByPosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
