/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException conformance: exception hierarchy and construction.
 * @run main FastInfosetExceptionTest
 */

public class FastInfosetExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException x = new com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
