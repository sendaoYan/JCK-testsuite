/*
 * @test
 * @summary java.awt.color.ProfileDataException conformance: white-box structure plus black-box behavior.
 * @run main ProfileDataExceptionTest
 */

import java.awt.color.*;
import java.lang.reflect.*;

public class ProfileDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.ProfileDataException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ProfileDataException e = new ProfileDataException("invalid TRC");
        check("invalid TRC".equals(e.getMessage()), "String ctor message preserved");
        check(e.getCause() == null, "cause null");
        check(e instanceof RuntimeException, "instanceof RuntimeException");

        // real trigger: sRGB TRC is a table, so getGamma(red) throws ProfileDataException
        ICC_ProfileRGB rgb = (ICC_ProfileRGB) ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        boolean threw = false;
        try { rgb.getGamma(ICC_ProfileRGB.REDCOMPONENT); }
        catch (ProfileDataException ex) { threw = true; }
        check(threw, "ICC_ProfileRGB.getGamma on table TRC throws ProfileDataException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
