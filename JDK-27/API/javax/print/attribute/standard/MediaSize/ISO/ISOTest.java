/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.ISO conformance: white-box structure + black-box behavior.
 * @run main ISOTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ISOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize$ISO");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        int n = 0; for (Field f : c.getDeclaredFields()) if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && f.getType() == MediaSize.class) n++;
        check(n > 0, "has MediaSize constants");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize s = MediaSize.ISO.A4;
        check(s != null, "constant non-null");
        check(s.getCategory() == MediaSize.class && "media-size".equals(s.getName()), "getCategory/getName");
        check(s.getMediaSizeName() != null, "getMediaSizeName non-null");
        check(s.getX(MediaSize.MM) == 210.0f && s.getY(MediaSize.MM) == 297.0f, "MediaSize.ISO.A4 exact size");
        check("210.0x297.0 mm".equals(s.toString(MediaSize.MM, "mm")), "MediaSize.ISO.A4 toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
