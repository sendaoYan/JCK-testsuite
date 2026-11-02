/*
 * @test
 * @summary sun.security.util.PropertyExpander.ExpandException conformance: type shape + behavioral (headless-safe).
 * @run main ExpandExceptionTest
 */

public class ExpandExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.PropertyExpander$ExpandException", false, ExpandExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
        sun.security.util.PropertyExpander.ExpandException ee = new sun.security.util.PropertyExpander.ExpandException("x");
        check(ee instanceof java.security.GeneralSecurityException, "ExpandException instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
