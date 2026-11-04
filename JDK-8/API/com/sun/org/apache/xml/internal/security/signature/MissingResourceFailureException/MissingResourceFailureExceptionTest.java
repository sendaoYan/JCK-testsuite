/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.MissingResourceFailureException conformance: class hierarchy and members.
 * @run main MissingResourceFailureExceptionTest
 */
public class MissingResourceFailureExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.MissingResourceFailureException", false, MissingResourceFailureExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.signature.XMLSignatureException"), "superclass com.sun.org.apache.xml.internal.security.signature.XMLSignatureException");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setReference"), "method setReference");
        check(mn.contains("getReference"), "method getReference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
