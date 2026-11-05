/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.JIS conformance: white-box structure + black-box behavior.
 * @run main JISTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JISTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize$JIS");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        int n = 0; for (Field f : c.getDeclaredFields()) if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && f.getType() == MediaSize.class) n++;
        check(n > 0, "has MediaSize constants");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize s = MediaSize.JIS.B5;
        check(s != null, "constant non-null");
        check(s.getCategory() == MediaSize.class && "media-size".equals(s.getName()), "getCategory/getName");
        check(s.getMediaSizeName() != null, "getMediaSizeName non-null");
        check(s.getX(MediaSize.MM) == 182.0f && s.getY(MediaSize.MM) == 257.0f, "MediaSize.JIS.B5 exact size");
        check("182.0x257.0 mm".equals(s.toString(MediaSize.MM, "mm")), "MediaSize.JIS.B5 toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.JIS.B0.getX(MediaSize.MM) == 1030f && MediaSize.JIS.B0.getY(MediaSize.MM) == 1456f, "JIS.B0 1030x1456 mm");
        check(MediaSize.JIS.B1.getX(MediaSize.MM) == 728f && MediaSize.JIS.B1.getY(MediaSize.MM) == 1030f, "JIS.B1 728x1030 mm");
        check(MediaSize.JIS.B2.getX(MediaSize.MM) == 515f && MediaSize.JIS.B2.getY(MediaSize.MM) == 728f, "JIS.B2 515x728 mm");
        check(MediaSize.JIS.B3.getX(MediaSize.MM) == 364f && MediaSize.JIS.B3.getY(MediaSize.MM) == 515f, "JIS.B3 364x515 mm");
        check(MediaSize.JIS.B4.getX(MediaSize.MM) == 257f && MediaSize.JIS.B4.getY(MediaSize.MM) == 364f, "JIS.B4 257x364 mm");
        check(MediaSize.JIS.B5.getX(MediaSize.MM) == 182f && MediaSize.JIS.B5.getY(MediaSize.MM) == 257f, "JIS.B5 182x257 mm");
        check(MediaSize.JIS.B6.getX(MediaSize.MM) == 128f && MediaSize.JIS.B6.getY(MediaSize.MM) == 182f, "JIS.B6 128x182 mm");
        check(MediaSize.JIS.B7.getX(MediaSize.MM) == 91f && MediaSize.JIS.B7.getY(MediaSize.MM) == 128f, "JIS.B7 91x128 mm");
        check(MediaSize.JIS.B8.getX(MediaSize.MM) == 64f && MediaSize.JIS.B8.getY(MediaSize.MM) == 91f, "JIS.B8 64x91 mm");
        check(MediaSize.JIS.B9.getX(MediaSize.MM) == 45f && MediaSize.JIS.B9.getY(MediaSize.MM) == 64f, "JIS.B9 45x64 mm");
        check(MediaSize.JIS.B10.getX(MediaSize.MM) == 32f && MediaSize.JIS.B10.getY(MediaSize.MM) == 45f, "JIS.B10 32x45 mm");
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
