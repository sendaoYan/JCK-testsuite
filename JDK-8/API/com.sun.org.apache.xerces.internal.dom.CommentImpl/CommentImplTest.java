/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.CommentImpl conformance: class hierarchy and members.
 * @run main CommentImplTest
 */
public class CommentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.CommentImpl", false, CommentImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.CharacterDataImpl"), "superclass com.sun.org.apache.xerces.internal.dom.CharacterDataImpl");
        check(hasInterface(c, "org.w3c.dom.CharacterData"), "has interface org.w3c.dom.CharacterData");
        check(hasInterface(c, "org.w3c.dom.Comment"), "has interface org.w3c.dom.Comment");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getNodeName"), "method getNodeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
