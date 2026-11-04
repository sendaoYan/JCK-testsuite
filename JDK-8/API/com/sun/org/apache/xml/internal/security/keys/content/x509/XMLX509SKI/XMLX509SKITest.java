/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509SKI conformance: class hierarchy and members.
 * @run main XMLX509SKITest
 */
public class XMLX509SKITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509SKI", false, XMLX509SKITest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509DataContent"), "has interface com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509DataContent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSKIBytes"), "method getSKIBytes");
        check(mn.contains("getSKIBytesFromCert"), "method getSKIBytesFromCert");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
