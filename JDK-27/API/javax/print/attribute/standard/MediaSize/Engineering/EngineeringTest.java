/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.Engineering conformance: white-box structure + black-box behavior.
 * @run main EngineeringTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class EngineeringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize$Engineering");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        int n = 0; for (Field f : c.getDeclaredFields()) if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && f.getType() == MediaSize.class) n++;
        check(n > 0, "has MediaSize constants");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize s = MediaSize.Engineering.A;
        check(s != null, "constant non-null");
        check(s.getCategory() == MediaSize.class && "media-size".equals(s.getName()), "getCategory/getName");
        check(s.getMediaSizeName() != null, "getMediaSizeName non-null");
        check(s.getX(MediaSize.INCH) == 8.5f && s.getY(MediaSize.INCH) == 11.0f, "MediaSize.Engineering.A exact size");
        check("8.5x11.0 in".equals(s.toString(MediaSize.INCH, "in")), "MediaSize.Engineering.A toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.Engineering.A.getX(MediaSize.MM) == 215.9f && MediaSize.Engineering.A.getY(MediaSize.MM) == 279.4f, "Engineering.A 215.9x279.4 mm");
        check(MediaSize.Engineering.B.getX(MediaSize.MM) == 279.4f && MediaSize.Engineering.B.getY(MediaSize.MM) == 431.8f, "Engineering.B 279.4x431.8 mm");
        check(MediaSize.Engineering.C.getX(MediaSize.MM) == 431.8f && MediaSize.Engineering.C.getY(MediaSize.MM) == 558.8f, "Engineering.C 431.8x558.8 mm");
        check(MediaSize.Engineering.D.getX(MediaSize.MM) == 558.8f && MediaSize.Engineering.D.getY(MediaSize.MM) == 863.6f, "Engineering.D 558.8x863.6 mm");
        check(MediaSize.Engineering.E.getX(MediaSize.MM) == 863.6f && MediaSize.Engineering.E.getY(MediaSize.MM) == 1117.6f, "Engineering.E 863.6x1117.6 mm");
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
