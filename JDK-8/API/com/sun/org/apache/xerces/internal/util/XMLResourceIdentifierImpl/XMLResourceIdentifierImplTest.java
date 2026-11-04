/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl conformance: class hierarchy and members.
 * @run main XMLResourceIdentifierImplTest
 */
public class XMLResourceIdentifierImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl", false, XMLResourceIdentifierImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier"), "has interface com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setValues"), "method setValues");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("setLiteralSystemId"), "method setLiteralSystemId");
        check(mn.contains("setBaseSystemId"), "method setBaseSystemId");
        check(mn.contains("setExpandedSystemId"), "method setExpandedSystemId");
        check(mn.contains("setNamespace"), "method setNamespace");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
