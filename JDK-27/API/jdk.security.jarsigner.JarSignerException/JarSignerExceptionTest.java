/*
 * @test
 * @summary jdk.security.jarsigner.JarSignerException conformance: jarsigner exception with cause ctor.
 * @run main JarSignerExceptionTest
 */

import jdk.security.jarsigner.JarSignerException;

public class JarSignerExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Throwable c = new Throwable("cause");
        JarSignerException e = new JarSignerException("msg", c);
        check("msg".equals(e.getMessage()), "message ctor");
        check(e.getCause() == c, "cause ctor");
        check(e instanceof RuntimeException, "is RuntimeException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
