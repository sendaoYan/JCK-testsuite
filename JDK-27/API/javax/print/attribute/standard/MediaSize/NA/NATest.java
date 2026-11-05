/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.NA conformance: white-box structure + black-box behavior.
 * @run main NATest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class NATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize$NA");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        int n = 0; for (Field f : c.getDeclaredFields()) if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && f.getType() == MediaSize.class) n++;
        check(n > 0, "has MediaSize constants");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize s = MediaSize.NA.LETTER;
        check(s != null, "constant non-null");
        check(s.getCategory() == MediaSize.class && "media-size".equals(s.getName()), "getCategory/getName");
        check(s.getMediaSizeName() != null, "getMediaSizeName non-null");
        check(s.getX(MediaSize.INCH) == 8.5f && s.getY(MediaSize.INCH) == 11.0f, "MediaSize.NA.LETTER exact size");
        check("8.5x11.0 in".equals(s.toString(MediaSize.INCH, "in")), "MediaSize.NA.LETTER toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
