/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.ISO conformance: media size constants.
 * @run main ISOTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ISOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "ISO");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_A0 = c.getDeclaredField("A0");
        check(java.lang.reflect.Modifier.isStatic(f_A0.getModifiers()), "A0 is static");
        check(f_A0.getType() == javax.print.attribute.standard.MediaSize.class, "A0 type MediaSize");
        check(f_A0.get(null) != null, "A0 non-null");
        java.lang.reflect.Field f_A1 = c.getDeclaredField("A1");
        check(java.lang.reflect.Modifier.isStatic(f_A1.getModifiers()), "A1 is static");
        check(f_A1.getType() == javax.print.attribute.standard.MediaSize.class, "A1 type MediaSize");
        check(f_A1.get(null) != null, "A1 non-null");
        java.lang.reflect.Field f_A2 = c.getDeclaredField("A2");
        check(java.lang.reflect.Modifier.isStatic(f_A2.getModifiers()), "A2 is static");
        check(f_A2.getType() == javax.print.attribute.standard.MediaSize.class, "A2 type MediaSize");
        check(f_A2.get(null) != null, "A2 non-null");
        java.lang.reflect.Field f_A3 = c.getDeclaredField("A3");
        check(java.lang.reflect.Modifier.isStatic(f_A3.getModifiers()), "A3 is static");
        check(f_A3.getType() == javax.print.attribute.standard.MediaSize.class, "A3 type MediaSize");
        check(f_A3.get(null) != null, "A3 non-null");
        java.lang.reflect.Field f_A4 = c.getDeclaredField("A4");
        check(java.lang.reflect.Modifier.isStatic(f_A4.getModifiers()), "A4 is static");
        check(f_A4.getType() == javax.print.attribute.standard.MediaSize.class, "A4 type MediaSize");
        check(f_A4.get(null) != null, "A4 non-null");
        java.lang.reflect.Field f_A5 = c.getDeclaredField("A5");
        check(java.lang.reflect.Modifier.isStatic(f_A5.getModifiers()), "A5 is static");
        check(f_A5.getType() == javax.print.attribute.standard.MediaSize.class, "A5 type MediaSize");
        check(f_A5.get(null) != null, "A5 non-null");
        java.lang.reflect.Field f_A6 = c.getDeclaredField("A6");
        check(java.lang.reflect.Modifier.isStatic(f_A6.getModifiers()), "A6 is static");
        check(f_A6.getType() == javax.print.attribute.standard.MediaSize.class, "A6 type MediaSize");
        check(f_A6.get(null) != null, "A6 non-null");
        java.lang.reflect.Field f_A7 = c.getDeclaredField("A7");
        check(java.lang.reflect.Modifier.isStatic(f_A7.getModifiers()), "A7 is static");
        check(f_A7.getType() == javax.print.attribute.standard.MediaSize.class, "A7 type MediaSize");
        check(f_A7.get(null) != null, "A7 non-null");
        java.lang.reflect.Field f_A8 = c.getDeclaredField("A8");
        check(java.lang.reflect.Modifier.isStatic(f_A8.getModifiers()), "A8 is static");
        check(f_A8.getType() == javax.print.attribute.standard.MediaSize.class, "A8 type MediaSize");
        check(f_A8.get(null) != null, "A8 non-null");
        java.lang.reflect.Field f_A9 = c.getDeclaredField("A9");
        check(java.lang.reflect.Modifier.isStatic(f_A9.getModifiers()), "A9 is static");
        check(f_A9.getType() == javax.print.attribute.standard.MediaSize.class, "A9 type MediaSize");
        check(f_A9.get(null) != null, "A9 non-null");
        java.lang.reflect.Field f_A10 = c.getDeclaredField("A10");
        check(java.lang.reflect.Modifier.isStatic(f_A10.getModifiers()), "A10 is static");
        check(f_A10.getType() == javax.print.attribute.standard.MediaSize.class, "A10 type MediaSize");
        check(f_A10.get(null) != null, "A10 non-null");
        java.lang.reflect.Field f_B0 = c.getDeclaredField("B0");
        check(java.lang.reflect.Modifier.isStatic(f_B0.getModifiers()), "B0 is static");
        check(f_B0.getType() == javax.print.attribute.standard.MediaSize.class, "B0 type MediaSize");
        check(f_B0.get(null) != null, "B0 non-null");
        java.lang.reflect.Field f_B1 = c.getDeclaredField("B1");
        check(java.lang.reflect.Modifier.isStatic(f_B1.getModifiers()), "B1 is static");
        check(f_B1.getType() == javax.print.attribute.standard.MediaSize.class, "B1 type MediaSize");
        check(f_B1.get(null) != null, "B1 non-null");
        java.lang.reflect.Field f_B2 = c.getDeclaredField("B2");
        check(java.lang.reflect.Modifier.isStatic(f_B2.getModifiers()), "B2 is static");
        check(f_B2.getType() == javax.print.attribute.standard.MediaSize.class, "B2 type MediaSize");
        check(f_B2.get(null) != null, "B2 non-null");
        java.lang.reflect.Field f_B3 = c.getDeclaredField("B3");
        check(java.lang.reflect.Modifier.isStatic(f_B3.getModifiers()), "B3 is static");
        check(f_B3.getType() == javax.print.attribute.standard.MediaSize.class, "B3 type MediaSize");
        check(f_B3.get(null) != null, "B3 non-null");
        java.lang.reflect.Field f_B4 = c.getDeclaredField("B4");
        check(java.lang.reflect.Modifier.isStatic(f_B4.getModifiers()), "B4 is static");
        check(f_B4.getType() == javax.print.attribute.standard.MediaSize.class, "B4 type MediaSize");
        check(f_B4.get(null) != null, "B4 non-null");
        java.lang.reflect.Field f_B5 = c.getDeclaredField("B5");
        check(java.lang.reflect.Modifier.isStatic(f_B5.getModifiers()), "B5 is static");
        check(f_B5.getType() == javax.print.attribute.standard.MediaSize.class, "B5 type MediaSize");
        check(f_B5.get(null) != null, "B5 non-null");
        java.lang.reflect.Field f_B6 = c.getDeclaredField("B6");
        check(java.lang.reflect.Modifier.isStatic(f_B6.getModifiers()), "B6 is static");
        check(f_B6.getType() == javax.print.attribute.standard.MediaSize.class, "B6 type MediaSize");
        check(f_B6.get(null) != null, "B6 non-null");
        java.lang.reflect.Field f_B7 = c.getDeclaredField("B7");
        check(java.lang.reflect.Modifier.isStatic(f_B7.getModifiers()), "B7 is static");
        check(f_B7.getType() == javax.print.attribute.standard.MediaSize.class, "B7 type MediaSize");
        check(f_B7.get(null) != null, "B7 non-null");
        java.lang.reflect.Field f_B8 = c.getDeclaredField("B8");
        check(java.lang.reflect.Modifier.isStatic(f_B8.getModifiers()), "B8 is static");
        check(f_B8.getType() == javax.print.attribute.standard.MediaSize.class, "B8 type MediaSize");
        check(f_B8.get(null) != null, "B8 non-null");
        java.lang.reflect.Field f_B9 = c.getDeclaredField("B9");
        check(java.lang.reflect.Modifier.isStatic(f_B9.getModifiers()), "B9 is static");
        check(f_B9.getType() == javax.print.attribute.standard.MediaSize.class, "B9 type MediaSize");
        check(f_B9.get(null) != null, "B9 non-null");
        java.lang.reflect.Field f_B10 = c.getDeclaredField("B10");
        check(java.lang.reflect.Modifier.isStatic(f_B10.getModifiers()), "B10 is static");
        check(f_B10.getType() == javax.print.attribute.standard.MediaSize.class, "B10 type MediaSize");
        check(f_B10.get(null) != null, "B10 non-null");
        java.lang.reflect.Field f_C3 = c.getDeclaredField("C3");
        check(java.lang.reflect.Modifier.isStatic(f_C3.getModifiers()), "C3 is static");
        check(f_C3.getType() == javax.print.attribute.standard.MediaSize.class, "C3 type MediaSize");
        check(f_C3.get(null) != null, "C3 non-null");
        java.lang.reflect.Field f_C4 = c.getDeclaredField("C4");
        check(java.lang.reflect.Modifier.isStatic(f_C4.getModifiers()), "C4 is static");
        check(f_C4.getType() == javax.print.attribute.standard.MediaSize.class, "C4 type MediaSize");
        check(f_C4.get(null) != null, "C4 non-null");
        java.lang.reflect.Field f_C5 = c.getDeclaredField("C5");
        check(java.lang.reflect.Modifier.isStatic(f_C5.getModifiers()), "C5 is static");
        check(f_C5.getType() == javax.print.attribute.standard.MediaSize.class, "C5 type MediaSize");
        check(f_C5.get(null) != null, "C5 non-null");
        java.lang.reflect.Field f_C6 = c.getDeclaredField("C6");
        check(java.lang.reflect.Modifier.isStatic(f_C6.getModifiers()), "C6 is static");
        check(f_C6.getType() == javax.print.attribute.standard.MediaSize.class, "C6 type MediaSize");
        check(f_C6.get(null) != null, "C6 non-null");
        java.lang.reflect.Field f_DESIGNATED_LONG = c.getDeclaredField("DESIGNATED_LONG");
        check(java.lang.reflect.Modifier.isStatic(f_DESIGNATED_LONG.getModifiers()), "DESIGNATED_LONG is static");
        check(f_DESIGNATED_LONG.getType() == javax.print.attribute.standard.MediaSize.class, "DESIGNATED_LONG type MediaSize");
        check(f_DESIGNATED_LONG.get(null) != null, "DESIGNATED_LONG non-null");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.ISO.A0.getX(MediaSize.MM) == 841f && MediaSize.ISO.A0.getY(MediaSize.MM) == 1189f, "ISO.A0 841x1189 mm");
        check(MediaSize.ISO.A1.getX(MediaSize.MM) == 594f && MediaSize.ISO.A1.getY(MediaSize.MM) == 841f, "ISO.A1 594x841 mm");
        check(MediaSize.ISO.A2.getX(MediaSize.MM) == 420f && MediaSize.ISO.A2.getY(MediaSize.MM) == 594f, "ISO.A2 420x594 mm");
        check(MediaSize.ISO.A3.getX(MediaSize.MM) == 297f && MediaSize.ISO.A3.getY(MediaSize.MM) == 420f, "ISO.A3 297x420 mm");
        check(MediaSize.ISO.A4.getX(MediaSize.MM) == 210f && MediaSize.ISO.A4.getY(MediaSize.MM) == 297f, "ISO.A4 210x297 mm");
        check(MediaSize.ISO.A5.getX(MediaSize.MM) == 148f && MediaSize.ISO.A5.getY(MediaSize.MM) == 210f, "ISO.A5 148x210 mm");
        check(MediaSize.ISO.A6.getX(MediaSize.MM) == 105f && MediaSize.ISO.A6.getY(MediaSize.MM) == 148f, "ISO.A6 105x148 mm");
        check(MediaSize.ISO.A7.getX(MediaSize.MM) == 74f && MediaSize.ISO.A7.getY(MediaSize.MM) == 105f, "ISO.A7 74x105 mm");
        check(MediaSize.ISO.A8.getX(MediaSize.MM) == 52f && MediaSize.ISO.A8.getY(MediaSize.MM) == 74f, "ISO.A8 52x74 mm");
        check(MediaSize.ISO.A9.getX(MediaSize.MM) == 37f && MediaSize.ISO.A9.getY(MediaSize.MM) == 52f, "ISO.A9 37x52 mm");
        check(MediaSize.ISO.A10.getX(MediaSize.MM) == 26f && MediaSize.ISO.A10.getY(MediaSize.MM) == 37f, "ISO.A10 26x37 mm");
        check(MediaSize.ISO.B0.getX(MediaSize.MM) == 1000f && MediaSize.ISO.B0.getY(MediaSize.MM) == 1414f, "ISO.B0 1000x1414 mm");
        check(MediaSize.ISO.B1.getX(MediaSize.MM) == 707f && MediaSize.ISO.B1.getY(MediaSize.MM) == 1000f, "ISO.B1 707x1000 mm");
        check(MediaSize.ISO.B2.getX(MediaSize.MM) == 500f && MediaSize.ISO.B2.getY(MediaSize.MM) == 707f, "ISO.B2 500x707 mm");
        check(MediaSize.ISO.B3.getX(MediaSize.MM) == 353f && MediaSize.ISO.B3.getY(MediaSize.MM) == 500f, "ISO.B3 353x500 mm");
        check(MediaSize.ISO.B4.getX(MediaSize.MM) == 250f && MediaSize.ISO.B4.getY(MediaSize.MM) == 353f, "ISO.B4 250x353 mm");
        check(MediaSize.ISO.B5.getX(MediaSize.MM) == 176f && MediaSize.ISO.B5.getY(MediaSize.MM) == 250f, "ISO.B5 176x250 mm");
        check(MediaSize.ISO.B6.getX(MediaSize.MM) == 125f && MediaSize.ISO.B6.getY(MediaSize.MM) == 176f, "ISO.B6 125x176 mm");
        check(MediaSize.ISO.B7.getX(MediaSize.MM) == 88f && MediaSize.ISO.B7.getY(MediaSize.MM) == 125f, "ISO.B7 88x125 mm");
        check(MediaSize.ISO.B8.getX(MediaSize.MM) == 62f && MediaSize.ISO.B8.getY(MediaSize.MM) == 88f, "ISO.B8 62x88 mm");
        check(MediaSize.ISO.B9.getX(MediaSize.MM) == 44f && MediaSize.ISO.B9.getY(MediaSize.MM) == 62f, "ISO.B9 44x62 mm");
        check(MediaSize.ISO.B10.getX(MediaSize.MM) == 31f && MediaSize.ISO.B10.getY(MediaSize.MM) == 44f, "ISO.B10 31x44 mm");
        check(MediaSize.ISO.C3.getX(MediaSize.MM) == 324f && MediaSize.ISO.C3.getY(MediaSize.MM) == 458f, "ISO.C3 324x458 mm");
        check(MediaSize.ISO.C4.getX(MediaSize.MM) == 229f && MediaSize.ISO.C4.getY(MediaSize.MM) == 324f, "ISO.C4 229x324 mm");
        check(MediaSize.ISO.C5.getX(MediaSize.MM) == 162f && MediaSize.ISO.C5.getY(MediaSize.MM) == 229f, "ISO.C5 162x229 mm");
        check(MediaSize.ISO.C6.getX(MediaSize.MM) == 114f && MediaSize.ISO.C6.getY(MediaSize.MM) == 162f, "ISO.C6 114x162 mm");
        check(MediaSize.ISO.DESIGNATED_LONG.getX(MediaSize.MM) == 110f && MediaSize.ISO.DESIGNATED_LONG.getY(MediaSize.MM) == 220f, "ISO.DESIGNATED_LONG 110x220 mm");
        // at most 1/100 mm rounding tolerance on inch-derived conversions
        check(Math.abs(MediaSize.NA.LETTER.getX(MediaSize.INCH) - 8.5f) < 1e-5f, "NA.LETTER inch");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
