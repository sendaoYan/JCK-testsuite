/*
 * @test
 * @summary sun.security.jgss.GSSExceptionImpl conformance: safe value/exception behavior.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSExceptionImplTest
 */
public class GSSExceptionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        sun.security.jgss.GSSExceptionImpl e = new sun.security.jgss.GSSExceptionImpl(13, "boom");
        check(e.getMajor() == 13, "getMajor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
