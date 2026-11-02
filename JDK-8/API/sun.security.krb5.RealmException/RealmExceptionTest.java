/*
 * @test
 * @summary sun.security.krb5.RealmException conformance: type shape + behavioral (headless-safe).
 * @run main RealmExceptionTest
 */

public class RealmExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.RealmException", false, RealmExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.krb5.KrbException"), "superclass");
        sun.security.krb5.RealmException re = new sun.security.krb5.RealmException("realm");
        check(re.getMessage() != null, "RealmException message");
        check(re instanceof sun.security.krb5.KrbException, "RealmException instanceof KrbException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
