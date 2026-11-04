/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase conformance: abstract class hierarchy and members.
 * @run main DTMAxisIteratorBaseTest
 */
public class DTMAxisIteratorBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase", false, DTMAxisIteratorBaseTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.dtm.DTMAxisIterator"), "has interface com.sun.org.apache.xml.internal.dtm.DTMAxisIterator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getStartNode"), "method getStartNode");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("includeSelf"), "method includeSelf");
        check(mn.contains("getLast"), "method getLast");
        check(mn.contains("getPosition"), "method getPosition");
        check(mn.contains("isReverse"), "method isReverse");
        check(mn.contains("cloneIterator"), "method cloneIterator");
        check(mn.contains("returnNode"), "method returnNode");
        check(mn.contains("resetPosition"), "method resetPosition");
        check(mn.contains("isDocOrdered"), "method isDocOrdered");
        check(mn.contains("getAxis"), "method getAxis");
        check(mn.contains("setRestartable"), "method setRestartable");
        check(mn.contains("getNodeByPosition"), "method getNodeByPosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
