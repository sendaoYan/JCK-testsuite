/*
 * @test
 * @summary sun.security.util.Password conformance: type shape + behavioral (headless-safe).
 * @run main PasswordTest
 */

public class PasswordTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.Password", false, PasswordTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.io.ByteArrayInputStream pin = new java.io.ByteArrayInputStream("secret\n".getBytes("US-ASCII"));
        char[] pw = sun.security.util.Password.readPassword(pin);
        check(pw != null && pw.length == 6, "Password readPassword length");
        check(new String(pw).equals("secret"), "Password readPassword content");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
