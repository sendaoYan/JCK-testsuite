/*
 * @test
 * @summary java.awt.color.ICC_ProfileGray conformance: white-box structure plus black-box behavior.
 * @run main ICC_ProfileGrayTest
 */

import java.awt.color.*;
import java.lang.reflect.*;

public class ICC_ProfileGrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.ICC_ProfileGray");
        int m = c.getModifiers();
        // JDK-8: public class (non-final); JDK-9+ made it public final — asserted on the JDK-27 side.
        check(Modifier.isPublic(m) && !Modifier.isFinal(m), "public class, not final in JDK-8");
        check(ICC_Profile.class.isAssignableFrom(c), "extends ICC_Profile");
        check(hasMethod(c, "getMediaWhitePoint"), "getMediaWhitePoint()");
        check(hasMethod(c, "getGamma"), "getGamma()");
        check(hasMethod(c, "getTRC"), "getTRC()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ICC_Profile p = ICC_Profile.getInstance(ColorSpace.CS_GRAY);
        check(p instanceof ICC_ProfileGray, "getInstance(CS_GRAY) returns ICC_ProfileGray");
        ICC_ProfileGray gray = (ICC_ProfileGray) p;

        float[] wp = gray.getMediaWhitePoint();
        check(wp != null && wp.length == 3, "mediaWhitePoint 3 components");
        check(wp[0] > 0 && wp[1] > 0 && wp[2] > 0, "mediaWhitePoint positive");

        // built-in gray TRC is a single gamma = 1.0 (linear)
        check(gray.getGamma() == 1.0f, "gray getGamma == 1.0");

        // linear/gamma TRC means getTRC() throws ProfileDataException
        boolean threw = false;
        try { gray.getTRC(); }
        catch (ProfileDataException e) { threw = true; }
        check(threw, "gray getTRC throws ProfileDataException (linear TRC)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
