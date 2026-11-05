/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.NA conformance: media size constants.
 * @run main NATest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "NA");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_LETTER = c.getDeclaredField("LETTER");
        check(java.lang.reflect.Modifier.isStatic(f_LETTER.getModifiers()), "LETTER is static");
        check(f_LETTER.getType() == javax.print.attribute.standard.MediaSize.class, "LETTER type MediaSize");
        check(f_LETTER.get(null) != null, "LETTER non-null");
        java.lang.reflect.Field f_LEGAL = c.getDeclaredField("LEGAL");
        check(java.lang.reflect.Modifier.isStatic(f_LEGAL.getModifiers()), "LEGAL is static");
        check(f_LEGAL.getType() == javax.print.attribute.standard.MediaSize.class, "LEGAL type MediaSize");
        check(f_LEGAL.get(null) != null, "LEGAL non-null");
        java.lang.reflect.Field f_NA_5X7 = c.getDeclaredField("NA_5X7");
        check(java.lang.reflect.Modifier.isStatic(f_NA_5X7.getModifiers()), "NA_5X7 is static");
        check(f_NA_5X7.getType() == javax.print.attribute.standard.MediaSize.class, "NA_5X7 type MediaSize");
        check(f_NA_5X7.get(null) != null, "NA_5X7 non-null");
        java.lang.reflect.Field f_NA_8X10 = c.getDeclaredField("NA_8X10");
        check(java.lang.reflect.Modifier.isStatic(f_NA_8X10.getModifiers()), "NA_8X10 is static");
        check(f_NA_8X10.getType() == javax.print.attribute.standard.MediaSize.class, "NA_8X10 type MediaSize");
        check(f_NA_8X10.get(null) != null, "NA_8X10 non-null");
        java.lang.reflect.Field f_NA_NUMBER_9_ENVELOPE = c.getDeclaredField("NA_NUMBER_9_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_9_ENVELOPE.getModifiers()), "NA_NUMBER_9_ENVELOPE is static");
        check(f_NA_NUMBER_9_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_9_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_9_ENVELOPE.get(null) != null, "NA_NUMBER_9_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_10_ENVELOPE = c.getDeclaredField("NA_NUMBER_10_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_10_ENVELOPE.getModifiers()), "NA_NUMBER_10_ENVELOPE is static");
        check(f_NA_NUMBER_10_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_10_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_10_ENVELOPE.get(null) != null, "NA_NUMBER_10_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_11_ENVELOPE = c.getDeclaredField("NA_NUMBER_11_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_11_ENVELOPE.getModifiers()), "NA_NUMBER_11_ENVELOPE is static");
        check(f_NA_NUMBER_11_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_11_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_11_ENVELOPE.get(null) != null, "NA_NUMBER_11_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_12_ENVELOPE = c.getDeclaredField("NA_NUMBER_12_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_12_ENVELOPE.getModifiers()), "NA_NUMBER_12_ENVELOPE is static");
        check(f_NA_NUMBER_12_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_12_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_12_ENVELOPE.get(null) != null, "NA_NUMBER_12_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_14_ENVELOPE = c.getDeclaredField("NA_NUMBER_14_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_14_ENVELOPE.getModifiers()), "NA_NUMBER_14_ENVELOPE is static");
        check(f_NA_NUMBER_14_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_14_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_14_ENVELOPE.get(null) != null, "NA_NUMBER_14_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_6X9_ENVELOPE = c.getDeclaredField("NA_6X9_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_6X9_ENVELOPE.getModifiers()), "NA_6X9_ENVELOPE is static");
        check(f_NA_6X9_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_6X9_ENVELOPE type MediaSize");
        check(f_NA_6X9_ENVELOPE.get(null) != null, "NA_6X9_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_7X9_ENVELOPE = c.getDeclaredField("NA_7X9_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_7X9_ENVELOPE.getModifiers()), "NA_7X9_ENVELOPE is static");
        check(f_NA_7X9_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_7X9_ENVELOPE type MediaSize");
        check(f_NA_7X9_ENVELOPE.get(null) != null, "NA_7X9_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_9x11_ENVELOPE = c.getDeclaredField("NA_9x11_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_9x11_ENVELOPE.getModifiers()), "NA_9x11_ENVELOPE is static");
        check(f_NA_9x11_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_9x11_ENVELOPE type MediaSize");
        check(f_NA_9x11_ENVELOPE.get(null) != null, "NA_9x11_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_9x12_ENVELOPE = c.getDeclaredField("NA_9x12_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_9x12_ENVELOPE.getModifiers()), "NA_9x12_ENVELOPE is static");
        check(f_NA_9x12_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_9x12_ENVELOPE type MediaSize");
        check(f_NA_9x12_ENVELOPE.get(null) != null, "NA_9x12_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_10x13_ENVELOPE = c.getDeclaredField("NA_10x13_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_10x13_ENVELOPE.getModifiers()), "NA_10x13_ENVELOPE is static");
        check(f_NA_10x13_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_10x13_ENVELOPE type MediaSize");
        check(f_NA_10x13_ENVELOPE.get(null) != null, "NA_10x13_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_10x14_ENVELOPE = c.getDeclaredField("NA_10x14_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_10x14_ENVELOPE.getModifiers()), "NA_10x14_ENVELOPE is static");
        check(f_NA_10x14_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_10x14_ENVELOPE type MediaSize");
        check(f_NA_10x14_ENVELOPE.get(null) != null, "NA_10x14_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_10X15_ENVELOPE = c.getDeclaredField("NA_10X15_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_10X15_ENVELOPE.getModifiers()), "NA_10X15_ENVELOPE is static");
        check(f_NA_10X15_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_10X15_ENVELOPE type MediaSize");
        check(f_NA_10X15_ENVELOPE.get(null) != null, "NA_10X15_ENVELOPE non-null");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.NA.LETTER.getX(MediaSize.MM) == 215.9f && MediaSize.NA.LETTER.getY(MediaSize.MM) == 279.4f, "NA.LETTER 215.9x279.4 mm");
        check(MediaSize.NA.LEGAL.getX(MediaSize.MM) == 215.9f && MediaSize.NA.LEGAL.getY(MediaSize.MM) == 355.6f, "NA.LEGAL 215.9x355.6 mm");
        check(MediaSize.NA.NA_5X7.getX(MediaSize.MM) == 127.0f && MediaSize.NA.NA_5X7.getY(MediaSize.MM) == 177.8f, "NA.NA_5X7 127.0x177.8 mm");
        check(MediaSize.NA.NA_8X10.getX(MediaSize.MM) == 203.2f && MediaSize.NA.NA_8X10.getY(MediaSize.MM) == 254.0f, "NA.NA_8X10 203.2x254.0 mm");
        check(MediaSize.NA.NA_NUMBER_9_ENVELOPE.getX(MediaSize.MM) == 98.425f && MediaSize.NA.NA_NUMBER_9_ENVELOPE.getY(MediaSize.MM) == 225.425f, "NA.NA_NUMBER_9_ENVELOPE 98.425x225.425 mm");
        check(MediaSize.NA.NA_NUMBER_10_ENVELOPE.getX(MediaSize.MM) == 104.775f && MediaSize.NA.NA_NUMBER_10_ENVELOPE.getY(MediaSize.MM) == 241.3f, "NA.NA_NUMBER_10_ENVELOPE 104.775x241.3 mm");
        check(MediaSize.NA.NA_NUMBER_11_ENVELOPE.getX(MediaSize.MM) == 114.3f && MediaSize.NA.NA_NUMBER_11_ENVELOPE.getY(MediaSize.MM) == 263.525f, "NA.NA_NUMBER_11_ENVELOPE 114.3x263.525 mm");
        check(MediaSize.NA.NA_NUMBER_12_ENVELOPE.getX(MediaSize.MM) == 120.65f && MediaSize.NA.NA_NUMBER_12_ENVELOPE.getY(MediaSize.MM) == 279.4f, "NA.NA_NUMBER_12_ENVELOPE 120.65x279.4 mm");
        check(MediaSize.NA.NA_NUMBER_14_ENVELOPE.getX(MediaSize.MM) == 127.0f && MediaSize.NA.NA_NUMBER_14_ENVELOPE.getY(MediaSize.MM) == 292.1f, "NA.NA_NUMBER_14_ENVELOPE 127.0x292.1 mm");
        check(MediaSize.NA.NA_6X9_ENVELOPE.getX(MediaSize.MM) == 152.4f && MediaSize.NA.NA_6X9_ENVELOPE.getY(MediaSize.MM) == 228.6f, "NA.NA_6X9_ENVELOPE 152.4x228.6 mm");
        check(MediaSize.NA.NA_7X9_ENVELOPE.getX(MediaSize.MM) == 177.8f && MediaSize.NA.NA_7X9_ENVELOPE.getY(MediaSize.MM) == 228.6f, "NA.NA_7X9_ENVELOPE 177.8x228.6 mm");
        check(MediaSize.NA.NA_9x11_ENVELOPE.getX(MediaSize.MM) == 228.6f && MediaSize.NA.NA_9x11_ENVELOPE.getY(MediaSize.MM) == 279.4f, "NA.NA_9x11_ENVELOPE 228.6x279.4 mm");
        check(MediaSize.NA.NA_9x12_ENVELOPE.getX(MediaSize.MM) == 228.6f && MediaSize.NA.NA_9x12_ENVELOPE.getY(MediaSize.MM) == 304.8f, "NA.NA_9x12_ENVELOPE 228.6x304.8 mm");
        check(MediaSize.NA.NA_10x13_ENVELOPE.getX(MediaSize.MM) == 254.0f && MediaSize.NA.NA_10x13_ENVELOPE.getY(MediaSize.MM) == 330.2f, "NA.NA_10x13_ENVELOPE 254.0x330.2 mm");
        check(MediaSize.NA.NA_10x14_ENVELOPE.getX(MediaSize.MM) == 254.0f && MediaSize.NA.NA_10x14_ENVELOPE.getY(MediaSize.MM) == 355.6f, "NA.NA_10x14_ENVELOPE 254.0x355.6 mm");
        check(MediaSize.NA.NA_10X15_ENVELOPE.getX(MediaSize.MM) == 254.0f && MediaSize.NA.NA_10X15_ENVELOPE.getY(MediaSize.MM) == 381.0f, "NA.NA_10X15_ENVELOPE 254.0x381.0 mm");
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
