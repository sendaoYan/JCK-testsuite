/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.URI.MalformedURIException conformance: class hierarchy and members.
 * @run main MalformedURIExceptionTest
 */
public class MalformedURIExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.URI$MalformedURIException", false, MalformedURIExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "superclass java.io.IOException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
