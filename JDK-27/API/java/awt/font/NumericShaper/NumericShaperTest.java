/*
 * @test
 * @summary java.awt.font.NumericShaper conformance: white-box structure plus black-box behavior.
 * @run main NumericShaperTest
 */

import java.awt.font.NumericShaper;
import java.io.Serializable;
import java.lang.reflect.*;

public class NumericShaperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.NumericShaper");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "getShaper", int.class), "getShaper(int)");
        check(hasMethod(c, "getContextualShaper", int.class), "getContextualShaper(int)");
        check(hasMethod(c, "shape", char[].class, int.class, int.class), "shape(char[],int,int)");
        check(hasMethod(c, "shape", char[].class, int.class, int.class, int.class), "shape(char[],int,int,int)");
        check(hasMethod(c, "isContextual"), "isContextual()");
        check(hasMethod(c, "getRanges"), "getRanges()");
        check(hasMethod(c, "getRangeSet"), "getRangeSet()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NumericShaper arabic = NumericShaper.getShaper(NumericShaper.ARABIC);
        check(!arabic.isContextual(), "getShaper non-contextual");
        check((arabic.getRanges() & NumericShaper.ARABIC) != 0, "getRanges has ARABIC");

        char[] digits = "0123456789".toCharArray();
        arabic.shape(digits, 0, digits.length);
        check(new String(digits).equals("٠١٢٣٤٥٦٧٨٩"),
              "ARABIC shaper converts ASCII digits to Arabic-Indic");

        // non-contextual shape over a subrange
        char[] mixed = "a1b2c3".toCharArray();
        NumericShaper.getShaper(NumericShaper.EASTERN_ARABIC).shape(mixed, 1, 4);
        check(mixed[0] == 'a' && mixed[5] == '3', "shape subrange leaves outside chars unchanged");

        // contextual shaper
        NumericShaper ctx = NumericShaper.getContextualShaper(NumericShaper.ARABIC);
        check(ctx.isContextual(), "contextual shaper isContextual");
        check(ctx.getRangeSet() != null && !ctx.getRangeSet().isEmpty(), "contextual getRangeSet non-empty");

        char[] ctxDigits = "123".toCharArray();
        ctx.shape(ctxDigits, 0, ctxDigits.length, NumericShaper.ARABIC);
        check(new String(ctxDigits).equals("١٢٣"), "contextual shape with ARABIC context");

        // equals / hashCode / toString
        NumericShaper arabic2 = NumericShaper.getShaper(NumericShaper.ARABIC);
        check(arabic.equals(arabic2) && arabic.hashCode() == arabic2.hashCode(), "equal shapers equal");
        check(arabic.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
