/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.SAXInputSource conformance: class hierarchy and members.
 * @run main SAXInputSourceTest
 */
public class SAXInputSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.SAXInputSource", false, SAXInputSourceTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource"), "superclass com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setXMLReader"), "method setXMLReader");
        check(mn.contains("getXMLReader"), "method getXMLReader");
        check(mn.contains("setInputSource"), "method setInputSource");
        check(mn.contains("getInputSource"), "method getInputSource");
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("setSystemId"), "method setSystemId");
        check(mn.contains("setByteStream"), "method setByteStream");
        check(mn.contains("setCharacterStream"), "method setCharacterStream");
        check(mn.contains("setEncoding"), "method setEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
