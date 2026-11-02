/*
 * @test
 * @summary sun.security.krb5.KrbException conformance: type shape + behavioral (headless-safe).
 * @run main KrbExceptionTest
 */

public class KrbExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.KrbException", false, KrbExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        sun.security.krb5.KrbException ke = new sun.security.krb5.KrbException("boom");
        check(ke.getMessage() != null, "KrbException message");
        check(ke.returnCodeSymbol() != null, "KrbException returnCodeSymbol");
        check(sun.security.krb5.KrbException.returnCodeSymbol(0) != null, "KrbException static returnCodeSymbol");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
