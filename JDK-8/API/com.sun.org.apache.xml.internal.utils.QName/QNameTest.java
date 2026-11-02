/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.QName conformance: class hierarchy and members.
 * @run main QNameTest
 */
public class QNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.QName", false, QNameTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNamespaceURI"), "method getNamespaceURI");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("toNamespacedString"), "method toNamespacedString");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getLocalPart"), "method getLocalPart");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getQNameFromString"), "method getQNameFromString");
        check(mn.contains("isXMLNSDecl"), "method isXMLNSDecl");
        check(mn.contains("getPrefixFromXMLNSDecl"), "method getPrefixFromXMLNSDecl");
        check(mn.contains("getPrefixPart"), "method getPrefixPart");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
