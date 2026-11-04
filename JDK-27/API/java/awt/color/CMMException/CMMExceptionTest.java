/*
 * @test
 * @summary java.awt.color.CMMException conformance: white-box structure plus black-box behavior.
 * @run main CMMExceptionTest
 */

import java.awt.color.CMMException;
import java.lang.reflect.*;

public class CMMExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.CMMException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CMMException e = new CMMException("CMM error");
        check("CMM error".equals(e.getMessage()), "String ctor message preserved");
        check(e.getCause() == null, "cause null");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
