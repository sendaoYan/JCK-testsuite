/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLLocator conformance: interface hierarchy and members.
 * @run main XMLLocatorTest
 */
public class XMLLocatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLLocator", false, XMLLocatorTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("getCharacterOffset"), "method getCharacterOffset");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("getXMLVersion"), "method getXMLVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
