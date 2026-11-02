/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator conformance: class hierarchy and members.
 * @run main DTMNodeIteratorTest
 */
public class DTMNodeIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator", false, DTMNodeIteratorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.traversal.NodeIterator"), "has interface org.w3c.dom.traversal.NodeIterator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDTMIterator"), "method getDTMIterator");
        check(mn.contains("detach"), "method detach");
        check(mn.contains("getExpandEntityReferences"), "method getExpandEntityReferences");
        check(mn.contains("getFilter"), "method getFilter");
        check(mn.contains("getRoot"), "method getRoot");
        check(mn.contains("getWhatToShow"), "method getWhatToShow");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("previousNode"), "method previousNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
