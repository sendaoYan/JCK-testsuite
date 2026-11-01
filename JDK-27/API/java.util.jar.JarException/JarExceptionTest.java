/*
 * @test
 * @summary java.util.jar.JarException conformance: constructors/inheritance.
 * @run main JarExceptionTest
 */

import java.io.IOException;
import java.util.jar.JarException;

public class JarExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJarException() {
        JarException e = new JarException();
        check(e.getMessage() == null, "no-arg message null");
        JarException e2 = new JarException("bad jar");
        check("bad jar".equals(e2.getMessage()), "message ctor");
        check(e2 instanceof IOException, "is an IOException");
    }

    public static void main(String[] args) {
        testJarException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
