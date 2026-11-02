/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.EmptyIterator conformance: class hierarchy and members.
 * @run main EmptyIteratorTest
 */
public class EmptyIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.EmptyIterator", false, EmptyIteratorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.dtm.DTMAxisIterator"), "has interface com.sun.org.apache.xml.internal.dtm.DTMAxisIterator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("next"), "method next");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getLast"), "method getLast");
        check(mn.contains("getPosition"), "method getPosition");
        check(mn.contains("setMark"), "method setMark");
        check(mn.contains("gotoMark"), "method gotoMark");
        check(mn.contains("setStartNode"), "method setStartNode");
        check(mn.contains("getStartNode"), "method getStartNode");
        check(mn.contains("isReverse"), "method isReverse");
        check(mn.contains("cloneIterator"), "method cloneIterator");
        check(mn.contains("setRestartable"), "method setRestartable");
        check(mn.contains("getNodeByPosition"), "method getNodeByPosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
