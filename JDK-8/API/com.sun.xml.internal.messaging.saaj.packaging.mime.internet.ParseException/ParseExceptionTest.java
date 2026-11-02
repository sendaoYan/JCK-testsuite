/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException conformance: exception hierarchy and construction.
 * @run main ParseExceptionTest
 */

public class ParseExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException x = new com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check("com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
