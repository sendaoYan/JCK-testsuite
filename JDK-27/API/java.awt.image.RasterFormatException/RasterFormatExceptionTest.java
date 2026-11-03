/*
 * @test
 * @summary java.awt.image.RasterFormatException conformance: white-box structure plus black-box behavior.
 * @run main RasterFormatExceptionTest
 */

import java.awt.image.RasterFormatException;
import java.lang.reflect.*;

public class RasterFormatExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RasterFormatException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RasterFormatException e = new RasterFormatException("bad raster");
        check(e.getMessage().equals("bad raster"), "getMessage");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e.getCause() == null, "no cause by default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
