/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.NodeVector conformance: class hierarchy and members.
 * @run main NodeVectorTest
 */
public class NodeVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.NodeVector", false, NodeVectorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        check(hasInterface(c, "java.lang.Cloneable"), "has interface java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clone"), "method clone");
        check(mn.contains("size"), "method size");
        check(mn.contains("addElement"), "method addElement");
        check(mn.contains("push"), "method push");
        check(mn.contains("pop"), "method pop");
        check(mn.contains("popAndTop"), "method popAndTop");
        check(mn.contains("popQuick"), "method popQuick");
        check(mn.contains("peepOrNull"), "method peepOrNull");
        check(mn.contains("pushPair"), "method pushPair");
        check(mn.contains("popPair"), "method popPair");
        check(mn.contains("setTail"), "method setTail");
        check(mn.contains("setTailSub1"), "method setTailSub1");
        check(mn.contains("peepTail"), "method peepTail");
        check(mn.contains("peepTailSub1"), "method peepTailSub1");
        check(mn.contains("insertInOrder"), "method insertInOrder");
        check(mn.contains("insertElementAt"), "method insertElementAt");
        check(mn.contains("appendNodes"), "method appendNodes");
        check(mn.contains("removeAllElements"), "method removeAllElements");
        check(mn.contains("RemoveAllNoClear"), "method RemoveAllNoClear");
        check(mn.contains("removeElement"), "method removeElement");
        check(mn.contains("removeElementAt"), "method removeElementAt");
        check(mn.contains("setElementAt"), "method setElementAt");
        check(mn.contains("elementAt"), "method elementAt");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("indexOf"), "method indexOf");
        check(mn.contains("sort"), "method sort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
