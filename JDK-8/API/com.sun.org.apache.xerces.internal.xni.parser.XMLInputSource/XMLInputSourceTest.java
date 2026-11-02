/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource conformance: class hierarchy and members.
 * @run main XMLInputSourceTest
 */
public class XMLInputSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource", false, XMLInputSourceTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setPublicId"), "method setPublicId");
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("setSystemId"), "method setSystemId");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("setBaseSystemId"), "method setBaseSystemId");
        check(mn.contains("getBaseSystemId"), "method getBaseSystemId");
        check(mn.contains("setByteStream"), "method setByteStream");
        check(mn.contains("getByteStream"), "method getByteStream");
        check(mn.contains("setCharacterStream"), "method setCharacterStream");
        check(mn.contains("getCharacterStream"), "method getCharacterStream");
        check(mn.contains("setEncoding"), "method setEncoding");
        check(mn.contains("getEncoding"), "method getEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
