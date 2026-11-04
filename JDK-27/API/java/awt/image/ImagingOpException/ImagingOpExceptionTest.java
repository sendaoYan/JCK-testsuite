/*
 * @test
 * @summary java.awt.image.ImagingOpException conformance: white-box structure plus black-box behavior.
 * @run main ImagingOpExceptionTest
 */

import java.awt.image.ImagingOpException;
import java.lang.reflect.*;

public class ImagingOpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImagingOpException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(!Exception.class.isAssignableFrom(c) || RuntimeException.class.isAssignableFrom(c), "unchecked");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImagingOpException e = new ImagingOpException("bad op");
        check(e.getMessage().equals("bad op"), "getMessage");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e instanceof Throwable, "instanceof Throwable");
        check(e.getCause() == null, "no cause by default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
