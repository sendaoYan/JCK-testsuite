/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException conformance: exception construction.
 * @run main WrongNumberArgsExceptionTest
 */

public class WrongNumberArgsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException x = new com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException("msg");
        check(java.lang.Throwable.class.isAssignableFrom(x.getClass()), "is Throwable");
        check(java.lang.Exception.class.isAssignableFrom(x.getClass()), "extends java.lang.Exception");
        check("msg".equals(x.getMessage()), "message equals msg");
        check(x.getCause() == null, "cause is null");
        check(x.toString() != null, "toString non-null");
        x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
