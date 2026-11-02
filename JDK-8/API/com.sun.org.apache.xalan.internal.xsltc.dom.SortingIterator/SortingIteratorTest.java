/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.SortingIterator conformance: reflection.
 * @run main SortingIteratorTest
 */

public class SortingIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.SortingIterator", false, SortingIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SortingIterator"), "simple name SortingIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "next"), "method next");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "setMark"), "method setMark");
        check(has(c, "gotoMark"), "method gotoMark");
        check(has(c, "cloneIterator"), "method cloneIterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
