/*
 * @test
 * @summary java.awt.color.ICC_ProfileRGB conformance: white-box structure plus black-box behavior.
 * @run main ICC_ProfileRGBTest
 */

import java.awt.color.*;
import java.lang.reflect.*;

public class ICC_ProfileRGBTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.ICC_ProfileRGB");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(ICC_Profile.class.isAssignableFrom(c), "extends ICC_Profile");
        for (String s : new String[]{"REDCOMPONENT","GREENCOMPONENT","BLUECOMPONENT"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + s);
        }
        check(hasMethod(c, "getMediaWhitePoint"), "getMediaWhitePoint()");
        check(hasMethod(c, "getMatrix"), "getMatrix()");
        check(hasMethod(c, "getGamma", int.class), "getGamma(int)");
        check(hasMethod(c, "getTRC", int.class), "getTRC(int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ICC_ProfileRGB.REDCOMPONENT == 0 && ICC_ProfileRGB.GREENCOMPONENT == 1
              && ICC_ProfileRGB.BLUECOMPONENT == 2, "component constant values");

        ICC_Profile p = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        check(p instanceof ICC_ProfileRGB, "getInstance(CS_sRGB) returns ICC_ProfileRGB");
        ICC_ProfileRGB rgb = (ICC_ProfileRGB) p;

        // media white point is D65 (0.9505, 1.0, 1.0890)
        float[] wp = rgb.getMediaWhitePoint();
        check(wp.length == 3, "mediaWhitePoint 3 components");
        check(close(wp[0], 0.9505f, 1e-3f) && close(wp[1], 1.0f, 1e-3f) && close(wp[2], 1.089f, 1e-3f),
              "sRGB white point ~D65");

        // matrix is 3x3
        float[][] matrix = rgb.getMatrix();
        check(matrix != null && matrix.length == 3 && matrix[0].length == 3
              && matrix[1].length == 3 && matrix[2].length == 3, "getMatrix 3x3");

        // sRGB TRC is a table, so getGamma throws ProfileDataException and getTRC returns table
        boolean gammaThrew = false;
        try { rgb.getGamma(ICC_ProfileRGB.REDCOMPONENT); }
        catch (ProfileDataException e) { gammaThrew = true; }
        check(gammaThrew, "getGamma(red) throws ProfileDataException (table TRC)");

        short[] trc = rgb.getTRC(ICC_ProfileRGB.REDCOMPONENT);
        check(trc != null && trc.length > 0, "getTRC(red) returns table");

        // invalid component throws IllegalArgumentException
        boolean badThrew = false;
        try { rgb.getGamma(99); }
        catch (IllegalArgumentException e) { badThrew = true; }
        check(badThrew, "getGamma(99) throws IllegalArgumentException");
    }

    static boolean close(float a, float b, float tol) { return Math.abs(a - b) < tol; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
