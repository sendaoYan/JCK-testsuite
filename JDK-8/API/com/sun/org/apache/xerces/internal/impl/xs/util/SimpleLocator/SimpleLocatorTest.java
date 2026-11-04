/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator conformance: class hierarchy and members.
 * @run main SimpleLocatorTest
 */
public class SimpleLocatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator", false, SimpleLocatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLLocator"), "has interface com.sun.org.apache.xerces.internal.xni.XMLLocator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setValues"), "method setValues");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("getCharacterOffset"), "method getCharacterOffset");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("setColumnNumber"), "method setColumnNumber");
        check(mn.contains("setLineNumber"), "method setLineNumber");
        check(mn.contains("setCharacterOffset"), "method setCharacterOffset");
        check(mn.contains("setBaseSystemId"), "method setBaseSystemId");
        check(mn.contains("setExpandedSystemId"), "method setExpandedSystemId");
        check(mn.contains("setLiteralSystemId"), "method setLiteralSystemId");
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("getXMLVersion"), "method getXMLVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
