/*
 * @test
 * @summary com.sun.xml.internal.txw2.IllegalAnnotationException conformance: exception hierarchy and construction.
 * @run main IllegalAnnotationExceptionTest
 */

public class IllegalAnnotationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.txw2.IllegalAnnotationException x = new com.sun.xml.internal.txw2.IllegalAnnotationException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("com.sun.xml.internal.txw2.TxwException".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.txw2.TxwException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
