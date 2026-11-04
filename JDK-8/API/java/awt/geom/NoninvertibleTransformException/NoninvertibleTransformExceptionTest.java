/*
 * @test
 * @summary java.awt.geom.NoninvertibleTransformException conformance: white-box structure plus black-box behavior.
 * @run main NoninvertibleTransformExceptionTest
 */

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.reflect.*;

public class NoninvertibleTransformExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.NoninvertibleTransformException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Exception.class.isAssignableFrom(c), "extends Exception");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception (not RuntimeException)");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NoninvertibleTransformException e = new NoninvertibleTransformException("singular matrix");
        check("singular matrix".equals(e.getMessage()), "String ctor message preserved");
        check(e.getCause() == null, "cause null");
        check(e instanceof Exception, "instanceof Exception");

        AffineTransform singular = AffineTransform.getScaleInstance(0, 1);
        boolean threwCreate = false;
        try { singular.createInverse(); }
        catch (NoninvertibleTransformException ex) { threwCreate = true; }
        check(threwCreate, "createInverse on singular matrix throws NoninvertibleTransformException");

        boolean threwInvert = false;
        try { singular.invert(); }
        catch (NoninvertibleTransformException ex) { threwInvert = true; }
        check(threwInvert, "invert on singular matrix throws NoninvertibleTransformException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
