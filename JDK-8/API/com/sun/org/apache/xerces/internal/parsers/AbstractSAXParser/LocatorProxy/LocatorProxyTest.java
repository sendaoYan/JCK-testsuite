/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser.LocatorProxy conformance: class hierarchy and members.
 * @run main LocatorProxyTest
 */
public class LocatorProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser$LocatorProxy", false, LocatorProxyTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.xml.sax.ext.Locator2"), "has interface org.xml.sax.ext.Locator2");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("getXMLVersion"), "method getXMLVersion");
        check(mn.contains("getEncoding"), "method getEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
