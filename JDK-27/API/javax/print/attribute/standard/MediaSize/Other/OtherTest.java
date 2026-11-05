/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.Other conformance: white-box structure + black-box behavior.
 * @run main OtherTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class OtherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSize$Other");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        int n = 0; for (Field f : c.getDeclaredFields()) if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && f.getType() == MediaSize.class) n++;
        check(n > 0, "has MediaSize constants");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MediaSize s = MediaSize.Other.EXECUTIVE;
        check(s != null, "constant non-null");
        check(s.getCategory() == MediaSize.class && "media-size".equals(s.getName()), "getCategory/getName");
        check(s.getMediaSizeName() != null, "getMediaSizeName non-null");
        check(s.getX(MediaSize.INCH) == 7.25f && s.getY(MediaSize.INCH) == 10.5f, "MediaSize.Other.EXECUTIVE exact size");
        check("7.25x10.5 in".equals(s.toString(MediaSize.INCH, "in")), "MediaSize.Other.EXECUTIVE toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full size table in mm / cross-unit consistency ----
    static void testContractDeep() throws Exception {
        check(MediaSize.Other.EXECUTIVE.getX(MediaSize.MM) == 184.15f && MediaSize.Other.EXECUTIVE.getY(MediaSize.MM) == 266.7f, "Other.EXECUTIVE 184.15x266.7 mm");
        check(MediaSize.Other.LEDGER.getX(MediaSize.MM) == 279.4f && MediaSize.Other.LEDGER.getY(MediaSize.MM) == 431.8f, "Other.LEDGER 279.4x431.8 mm");
        check(MediaSize.Other.TABLOID.getX(MediaSize.MM) == 279.4f && MediaSize.Other.TABLOID.getY(MediaSize.MM) == 431.8f, "Other.TABLOID 279.4x431.8 mm");
        check(MediaSize.Other.QUARTO.getX(MediaSize.MM) == 215.9f && MediaSize.Other.QUARTO.getY(MediaSize.MM) == 275.082f, "Other.QUARTO 215.9x275.082 mm");
        check(MediaSize.Other.FOLIO.getX(MediaSize.MM) == 215.9f && MediaSize.Other.FOLIO.getY(MediaSize.MM) == 330.2f, "Other.FOLIO 215.9x330.2 mm");
        check(MediaSize.Other.INVOICE.getX(MediaSize.MM) == 139.7f && MediaSize.Other.INVOICE.getY(MediaSize.MM) == 215.9f, "Other.INVOICE 139.7x215.9 mm");
        check(MediaSize.Other.ITALY_ENVELOPE.getX(MediaSize.MM) == 110.0f && MediaSize.Other.ITALY_ENVELOPE.getY(MediaSize.MM) == 230.0f, "Other.ITALY_ENVELOPE 110.0x230.0 mm");
        check(MediaSize.Other.MONARCH_ENVELOPE.getX(MediaSize.MM) == 98.298f && MediaSize.Other.MONARCH_ENVELOPE.getY(MediaSize.MM) == 190.5f, "Other.MONARCH_ENVELOPE 98.298x190.5 mm");
        check(MediaSize.Other.PERSONAL_ENVELOPE.getX(MediaSize.MM) == 92.075f && MediaSize.Other.PERSONAL_ENVELOPE.getY(MediaSize.MM) == 165.1f, "Other.PERSONAL_ENVELOPE 92.075x165.1 mm");
        check(MediaSize.Other.JAPANESE_POSTCARD.getX(MediaSize.MM) == 100.0f && MediaSize.Other.JAPANESE_POSTCARD.getY(MediaSize.MM) == 148.0f, "Other.JAPANESE_POSTCARD 100.0x148.0 mm");
        check(MediaSize.Other.JAPANESE_DOUBLE_POSTCARD.getX(MediaSize.MM) == 148.0f && MediaSize.Other.JAPANESE_DOUBLE_POSTCARD.getY(MediaSize.MM) == 200.0f, "Other.JAPANESE_DOUBLE_POSTCARD 148.0x200.0 mm");
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
