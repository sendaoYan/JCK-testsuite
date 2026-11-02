/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DOMOutputImpl conformance: class hierarchy and members.
 * @run main DOMOutputImplTest
 */
public class DOMOutputImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMOutputImpl", false, DOMOutputImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.ls.LSOutput"), "has interface org.w3c.dom.ls.LSOutput");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCharacterStream"), "method getCharacterStream");
        check(mn.contains("setCharacterStream"), "method setCharacterStream");
        check(mn.contains("getByteStream"), "method getByteStream");
        check(mn.contains("setByteStream"), "method setByteStream");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("setSystemId"), "method setSystemId");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("setEncoding"), "method setEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
