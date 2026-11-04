/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException conformance: class hierarchy and members.
 * @run main CanonicalizationExceptionTest
 */
public class CanonicalizationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException", false, CanonicalizationExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException"), "superclass com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
