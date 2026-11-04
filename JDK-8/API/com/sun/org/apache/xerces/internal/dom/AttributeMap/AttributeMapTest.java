/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.AttributeMap conformance: class hierarchy and members.
 * @run main AttributeMapTest
 */
public class AttributeMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.AttributeMap", false, AttributeMapTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl"), "superclass com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setNamedItem"), "method setNamedItem");
        check(mn.contains("setNamedItemNS"), "method setNamedItemNS");
        check(mn.contains("removeNamedItem"), "method removeNamedItem");
        check(mn.contains("safeRemoveNamedItem"), "method safeRemoveNamedItem");
        check(mn.contains("removeItem"), "method removeItem");
        check(mn.contains("internalRemoveNamedItem"), "method internalRemoveNamedItem");
        check(mn.contains("removeNamedItemNS"), "method removeNamedItemNS");
        check(mn.contains("safeRemoveNamedItemNS"), "method safeRemoveNamedItemNS");
        check(mn.contains("internalRemoveNamedItemNS"), "method internalRemoveNamedItemNS");
        check(mn.contains("cloneMap"), "method cloneMap");
        check(mn.contains("cloneContent"), "method cloneContent");
        check(mn.contains("moveSpecifiedAttributes"), "method moveSpecifiedAttributes");
        check(mn.contains("reconcileDefaults"), "method reconcileDefaults");
        check(mn.contains("addItem"), "method addItem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
