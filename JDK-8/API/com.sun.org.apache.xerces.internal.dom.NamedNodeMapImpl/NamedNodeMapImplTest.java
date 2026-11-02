/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl conformance: class hierarchy and members.
 * @run main NamedNodeMapImplTest
 */
public class NamedNodeMapImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl", false, NamedNodeMapImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.NamedNodeMap"), "has interface org.w3c.dom.NamedNodeMap");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("getNamedItem"), "method getNamedItem");
        check(mn.contains("getNamedItemNS"), "method getNamedItemNS");
        check(mn.contains("setNamedItem"), "method setNamedItem");
        check(mn.contains("setNamedItemNS"), "method setNamedItemNS");
        check(mn.contains("removeNamedItem"), "method removeNamedItem");
        check(mn.contains("removeNamedItemNS"), "method removeNamedItemNS");
        check(mn.contains("cloneMap"), "method cloneMap");
        check(mn.contains("cloneContent"), "method cloneContent");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        check(mn.contains("getReadOnly"), "method getReadOnly");
        check(mn.contains("setOwnerDocument"), "method setOwnerDocument");
        check(mn.contains("isReadOnly"), "method isReadOnly");
        check(mn.contains("changed"), "method changed");
        check(mn.contains("hasDefaults"), "method hasDefaults");
        check(mn.contains("findNamePoint"), "method findNamePoint");
        check(mn.contains("precedes"), "method precedes");
        check(mn.contains("removeItem"), "method removeItem");
        check(mn.contains("getItem"), "method getItem");
        check(mn.contains("addItem"), "method addItem");
        check(mn.contains("getNamedItemIndex"), "method getNamedItemIndex");
        check(mn.contains("removeAll"), "method removeAll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
