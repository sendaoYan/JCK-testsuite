/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.parser.XMLParseException conformance: class hierarchy and members.
 * @run main XMLParseExceptionTest
 */
public class XMLParseExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.parser.XMLParseException", false, XMLParseExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.xni.XNIException"), "superclass com.sun.org.apache.xerces.internal.xni.XNIException");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getExpandedSystemId"), "method getExpandedSystemId");
        check(mn.contains("getLiteralSystemId"), "method getLiteralSystemId");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        check(mn.contains("getCharacterOffset"), "method getCharacterOffset");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
