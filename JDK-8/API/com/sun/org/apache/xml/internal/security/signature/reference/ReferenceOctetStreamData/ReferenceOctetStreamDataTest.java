/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.reference.ReferenceOctetStreamData conformance: class hierarchy and members.
 * @run main ReferenceOctetStreamDataTest
 */
public class ReferenceOctetStreamDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.reference.ReferenceOctetStreamData", false, ReferenceOctetStreamDataTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.signature.reference.ReferenceData"), "has interface com.sun.org.apache.xml.internal.security.signature.reference.ReferenceData");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getOctetStream"), "method getOctetStream");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getMimeType"), "method getMimeType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
