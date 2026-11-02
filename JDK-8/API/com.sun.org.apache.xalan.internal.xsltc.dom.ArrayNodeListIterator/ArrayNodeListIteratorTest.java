/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.ArrayNodeListIterator conformance: reflection.
 * @run main ArrayNodeListIteratorTest
 */

public class ArrayNodeListIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.ArrayNodeListIterator", false, ArrayNodeListIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ArrayNodeListIterator"), "simple name ArrayNodeListIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xml.internal.dtm.DTMAxisIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMAxisIterator");
        check(has(c, "next"), "method next");
        check(has(c, "reset"), "method reset");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "setMark"), "method setMark");
        check(has(c, "gotoMark"), "method gotoMark");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "getStartNode"), "method getStartNode");
        check(has(c, "isReverse"), "method isReverse");
        check(has(c, "cloneIterator"), "method cloneIterator");
        check(has(c, "setRestartable"), "method setRestartable");
        check(has(c, "getNodeByPosition"), "method getNodeByPosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
