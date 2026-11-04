/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMNamedNodeMap conformance: class hierarchy and members.
 * @run main DTMNamedNodeMapTest
 */
public class DTMNamedNodeMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMNamedNodeMap", false, DTMNamedNodeMapTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.NamedNodeMap"), "has interface org.w3c.dom.NamedNodeMap");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("getNamedItem"), "method getNamedItem");
        check(mn.contains("item"), "method item");
        check(mn.contains("setNamedItem"), "method setNamedItem");
        check(mn.contains("removeNamedItem"), "method removeNamedItem");
        check(mn.contains("getNamedItemNS"), "method getNamedItemNS");
        check(mn.contains("setNamedItemNS"), "method setNamedItemNS");
        check(mn.contains("removeNamedItemNS"), "method removeNamedItemNS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
