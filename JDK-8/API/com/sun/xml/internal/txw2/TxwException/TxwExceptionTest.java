/*
 * @test
 * @summary com.sun.xml.internal.txw2.TxwException conformance: exception hierarchy and construction.
 * @run main TxwExceptionTest
 */

public class TxwExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.txw2.TxwException x = new com.sun.xml.internal.txw2.TxwException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("java.lang.RuntimeException".equals(c.getSuperclass().getName()), "extends java.lang.RuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
