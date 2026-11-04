/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xpath.XPathException conformance: exception hierarchy and construction.
 * @run main XPathExceptionTest
 */
public class XPathExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        java.lang.Throwable x = new com.sun.org.apache.xerces.internal.impl.xpath.XPathException("msg");
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xpath.XPathException", false, XPathExceptionTest.class.getClassLoader());
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.Exception", false, null).isAssignableFrom(c), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
