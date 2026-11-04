/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier conformance: interface hierarchy and members.
 * @run main XMLResourceIdentifierTest
 */
public class XMLResourceIdentifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier", false, XMLResourceIdentifierTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("setExpandedSystemId"), "method setExpandedSystemId");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("setLiteralSystemId"), "method setLiteralSystemId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("setBaseSystemId"), "method setBaseSystemId");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("setNamespace"), "method setNamespace");
        check(mn.contains("getNamespace"), "method getNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
