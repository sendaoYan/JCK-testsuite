/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.TreeWalkerImpl conformance: class hierarchy and members.
 * @run main TreeWalkerImplTest
 */
public class TreeWalkerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.TreeWalkerImpl", false, TreeWalkerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.traversal.TreeWalker"), "has interface org.w3c.dom.traversal.TreeWalker");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getRoot"), "method getRoot");
        check(mn.contains("getWhatToShow"), "method getWhatToShow");
        check(mn.contains("setWhatShow"), "method setWhatShow");
        check(mn.contains("getFilter"), "method getFilter");
        check(mn.contains("getExpandEntityReferences"), "method getExpandEntityReferences");
        check(mn.contains("getCurrentNode"), "method getCurrentNode");
        check(mn.contains("setCurrentNode"), "method setCurrentNode");
        check(mn.contains("parentNode"), "method parentNode");
        check(mn.contains("firstChild"), "method firstChild");
        check(mn.contains("lastChild"), "method lastChild");
        check(mn.contains("previousSibling"), "method previousSibling");
        check(mn.contains("nextSibling"), "method nextSibling");
        check(mn.contains("previousNode"), "method previousNode");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("getNextSibling"), "method getNextSibling");
        check(mn.contains("getPreviousSibling"), "method getPreviousSibling");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("acceptNode"), "method acceptNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
