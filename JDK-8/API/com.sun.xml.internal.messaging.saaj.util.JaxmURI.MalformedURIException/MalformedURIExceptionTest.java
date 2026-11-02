/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.JaxmURI.MalformedURIException conformance: class hierarchy and members.
 * @run main MalformedURIExceptionTest
 */

public class MalformedURIExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.JaxmURI$MalformedURIException", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.JaxmURI$MalformedURIException"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.IOException".equals(c.getSuperclass().getName()), "extends java.io.IOException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
