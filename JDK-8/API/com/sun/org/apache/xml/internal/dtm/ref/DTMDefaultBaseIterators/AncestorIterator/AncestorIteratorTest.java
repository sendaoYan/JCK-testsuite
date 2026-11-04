/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.AncestorIterator conformance: class hierarchy and members.
 * @run main AncestorIteratorTest
 */
public class AncestorIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators$AncestorIterator", false, AncestorIteratorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators$InternalAxisIteratorBase"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators$InternalAxisIteratorBase");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getStartNode"), "method getStartNode");
        check(mn.contains("isReverse"), "method isReverse");
        check(mn.contains("cloneIterator"), "method cloneIterator");
        check(mn.contains("setStartNode"), "method setStartNode");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("next"), "method next");
        check(mn.contains("setMark"), "method setMark");
        check(mn.contains("gotoMark"), "method gotoMark");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
