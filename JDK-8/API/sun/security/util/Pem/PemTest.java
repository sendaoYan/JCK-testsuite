/*
 * @test
 * @summary sun.security.util.Pem conformance: type shape + behavioral (headless-safe).
 * @run main PemTest
 */

public class PemTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.Pem", false, PemTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        byte[] pem = sun.security.util.Pem.decode("SGVsbG8=");
        check(pem != null && pem.length == 5, "Pem decode");
        check(new String(pem, "US-ASCII").equals("Hello"), "Pem decode content");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
