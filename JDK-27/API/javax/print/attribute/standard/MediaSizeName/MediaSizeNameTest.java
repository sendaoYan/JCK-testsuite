/*
 * @test
 * @summary javax.print.attribute.standard.MediaSizeName conformance: white-box structure + black-box behavior.
 * @run main MediaSizeNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MediaSizeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static final class MSN extends MediaSizeName {
        MSN(int v) { super(v); }
        String[] stringTable() { return super.getStringTable(); }
        javax.print.attribute.EnumSyntax[] enumValueTable() { return super.getEnumValueTable(); }
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MediaSizeName");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(Media.class.isAssignableFrom(c), "extends Media");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(MediaSizeName.ISO_A0.getCategory() == Media.class, "getCategory");
        check("media".equals(MediaSizeName.ISO_A0.getName()), "getName");
        check(MediaSizeName.ISO_A0.getValue() == 0, "ISO_A0 value == 0");
        check("iso-a0".equals(MediaSizeName.ISO_A0.toString()), "ISO_A0 toString");
        check(MediaSizeName.ISO_A1.getValue() == 1, "ISO_A1 value == 1");
        check("iso-a1".equals(MediaSizeName.ISO_A1.toString()), "ISO_A1 toString");
        check(MediaSizeName.ISO_A2.getValue() == 2, "ISO_A2 value == 2");
        check("iso-a2".equals(MediaSizeName.ISO_A2.toString()), "ISO_A2 toString");
        check(MediaSizeName.ISO_A3.getValue() == 3, "ISO_A3 value == 3");
        check("iso-a3".equals(MediaSizeName.ISO_A3.toString()), "ISO_A3 toString");
        check(MediaSizeName.ISO_A4.getValue() == 4, "ISO_A4 value == 4");
        check("iso-a4".equals(MediaSizeName.ISO_A4.toString()), "ISO_A4 toString");
        check(MediaSizeName.ISO_A5.getValue() == 5, "ISO_A5 value == 5");
        check("iso-a5".equals(MediaSizeName.ISO_A5.toString()), "ISO_A5 toString");
        check(MediaSizeName.ISO_A6.getValue() == 6, "ISO_A6 value == 6");
        check("iso-a6".equals(MediaSizeName.ISO_A6.toString()), "ISO_A6 toString");
        check(MediaSizeName.ISO_A7.getValue() == 7, "ISO_A7 value == 7");
        check("iso-a7".equals(MediaSizeName.ISO_A7.toString()), "ISO_A7 toString");
        check(MediaSizeName.ISO_A8.getValue() == 8, "ISO_A8 value == 8");
        check("iso-a8".equals(MediaSizeName.ISO_A8.toString()), "ISO_A8 toString");
        check(MediaSizeName.ISO_A9.getValue() == 9, "ISO_A9 value == 9");
        check("iso-a9".equals(MediaSizeName.ISO_A9.toString()), "ISO_A9 toString");
        check(MediaSizeName.ISO_A10.getValue() == 10, "ISO_A10 value == 10");
        check("iso-a10".equals(MediaSizeName.ISO_A10.toString()), "ISO_A10 toString");
        check(MediaSizeName.ISO_B0.getValue() == 11, "ISO_B0 value == 11");
        check("iso-b0".equals(MediaSizeName.ISO_B0.toString()), "ISO_B0 toString");
        check(MediaSizeName.ISO_B1.getValue() == 12, "ISO_B1 value == 12");
        check("iso-b1".equals(MediaSizeName.ISO_B1.toString()), "ISO_B1 toString");
        check(MediaSizeName.ISO_B2.getValue() == 13, "ISO_B2 value == 13");
        check("iso-b2".equals(MediaSizeName.ISO_B2.toString()), "ISO_B2 toString");
        check(MediaSizeName.ISO_B3.getValue() == 14, "ISO_B3 value == 14");
        check("iso-b3".equals(MediaSizeName.ISO_B3.toString()), "ISO_B3 toString");
        check(MediaSizeName.ISO_B4.getValue() == 15, "ISO_B4 value == 15");
        check("iso-b4".equals(MediaSizeName.ISO_B4.toString()), "ISO_B4 toString");
        check(MediaSizeName.ISO_B5.getValue() == 16, "ISO_B5 value == 16");
        check("iso-b5".equals(MediaSizeName.ISO_B5.toString()), "ISO_B5 toString");
        check(MediaSizeName.ISO_B6.getValue() == 17, "ISO_B6 value == 17");
        check("iso-b6".equals(MediaSizeName.ISO_B6.toString()), "ISO_B6 toString");
        check(MediaSizeName.ISO_B7.getValue() == 18, "ISO_B7 value == 18");
        check("iso-b7".equals(MediaSizeName.ISO_B7.toString()), "ISO_B7 toString");
        check(MediaSizeName.ISO_B8.getValue() == 19, "ISO_B8 value == 19");
        check("iso-b8".equals(MediaSizeName.ISO_B8.toString()), "ISO_B8 toString");
        check(MediaSizeName.ISO_B9.getValue() == 20, "ISO_B9 value == 20");
        check("iso-b9".equals(MediaSizeName.ISO_B9.toString()), "ISO_B9 toString");
        check(MediaSizeName.ISO_B10.getValue() == 21, "ISO_B10 value == 21");
        check("iso-b10".equals(MediaSizeName.ISO_B10.toString()), "ISO_B10 toString");
        check(MediaSizeName.JIS_B0.getValue() == 22, "JIS_B0 value == 22");
        check("jis-b0".equals(MediaSizeName.JIS_B0.toString()), "JIS_B0 toString");
        check(MediaSizeName.JIS_B1.getValue() == 23, "JIS_B1 value == 23");
        check("jis-b1".equals(MediaSizeName.JIS_B1.toString()), "JIS_B1 toString");
        check(MediaSizeName.JIS_B2.getValue() == 24, "JIS_B2 value == 24");
        check("jis-b2".equals(MediaSizeName.JIS_B2.toString()), "JIS_B2 toString");
        check(MediaSizeName.JIS_B3.getValue() == 25, "JIS_B3 value == 25");
        check("jis-b3".equals(MediaSizeName.JIS_B3.toString()), "JIS_B3 toString");
        check(MediaSizeName.JIS_B4.getValue() == 26, "JIS_B4 value == 26");
        check("jis-b4".equals(MediaSizeName.JIS_B4.toString()), "JIS_B4 toString");
        check(MediaSizeName.JIS_B5.getValue() == 27, "JIS_B5 value == 27");
        check("jis-b5".equals(MediaSizeName.JIS_B5.toString()), "JIS_B5 toString");
        check(MediaSizeName.JIS_B6.getValue() == 28, "JIS_B6 value == 28");
        check("jis-b6".equals(MediaSizeName.JIS_B6.toString()), "JIS_B6 toString");
        check(MediaSizeName.JIS_B7.getValue() == 29, "JIS_B7 value == 29");
        check("jis-b7".equals(MediaSizeName.JIS_B7.toString()), "JIS_B7 toString");
        check(MediaSizeName.JIS_B8.getValue() == 30, "JIS_B8 value == 30");
        check("jis-b8".equals(MediaSizeName.JIS_B8.toString()), "JIS_B8 toString");
        check(MediaSizeName.JIS_B9.getValue() == 31, "JIS_B9 value == 31");
        check("jis-b9".equals(MediaSizeName.JIS_B9.toString()), "JIS_B9 toString");
        check(MediaSizeName.JIS_B10.getValue() == 32, "JIS_B10 value == 32");
        check("jis-b10".equals(MediaSizeName.JIS_B10.toString()), "JIS_B10 toString");
        check(MediaSizeName.ISO_C0.getValue() == 33, "ISO_C0 value == 33");
        check("iso-c0".equals(MediaSizeName.ISO_C0.toString()), "ISO_C0 toString");
        check(MediaSizeName.ISO_C1.getValue() == 34, "ISO_C1 value == 34");
        check("iso-c1".equals(MediaSizeName.ISO_C1.toString()), "ISO_C1 toString");
        check(MediaSizeName.ISO_C2.getValue() == 35, "ISO_C2 value == 35");
        check("iso-c2".equals(MediaSizeName.ISO_C2.toString()), "ISO_C2 toString");
        check(MediaSizeName.ISO_C3.getValue() == 36, "ISO_C3 value == 36");
        check("iso-c3".equals(MediaSizeName.ISO_C3.toString()), "ISO_C3 toString");
        check(MediaSizeName.ISO_C4.getValue() == 37, "ISO_C4 value == 37");
        check("iso-c4".equals(MediaSizeName.ISO_C4.toString()), "ISO_C4 toString");
        check(MediaSizeName.ISO_C5.getValue() == 38, "ISO_C5 value == 38");
        check("iso-c5".equals(MediaSizeName.ISO_C5.toString()), "ISO_C5 toString");
        check(MediaSizeName.ISO_C6.getValue() == 39, "ISO_C6 value == 39");
        check("iso-c6".equals(MediaSizeName.ISO_C6.toString()), "ISO_C6 toString");
        check(MediaSizeName.NA_LETTER.getValue() == 40, "NA_LETTER value == 40");
        check("na-letter".equals(MediaSizeName.NA_LETTER.toString()), "NA_LETTER toString");
        check(MediaSizeName.NA_LEGAL.getValue() == 41, "NA_LEGAL value == 41");
        check("na-legal".equals(MediaSizeName.NA_LEGAL.toString()), "NA_LEGAL toString");
        check(MediaSizeName.EXECUTIVE.getValue() == 42, "EXECUTIVE value == 42");
        check("executive".equals(MediaSizeName.EXECUTIVE.toString()), "EXECUTIVE toString");
        check(MediaSizeName.LEDGER.getValue() == 43, "LEDGER value == 43");
        check("ledger".equals(MediaSizeName.LEDGER.toString()), "LEDGER toString");
        check(MediaSizeName.TABLOID.getValue() == 44, "TABLOID value == 44");
        check("tabloid".equals(MediaSizeName.TABLOID.toString()), "TABLOID toString");
        check(MediaSizeName.INVOICE.getValue() == 45, "INVOICE value == 45");
        check("invoice".equals(MediaSizeName.INVOICE.toString()), "INVOICE toString");
        check(MediaSizeName.FOLIO.getValue() == 46, "FOLIO value == 46");
        check("folio".equals(MediaSizeName.FOLIO.toString()), "FOLIO toString");
        check(MediaSizeName.QUARTO.getValue() == 47, "QUARTO value == 47");
        check("quarto".equals(MediaSizeName.QUARTO.toString()), "QUARTO toString");
        check(MediaSizeName.JAPANESE_POSTCARD.getValue() == 48, "JAPANESE_POSTCARD value == 48");
        check("japanese-postcard".equals(MediaSizeName.JAPANESE_POSTCARD.toString()), "JAPANESE_POSTCARD toString");
        check(MediaSizeName.JAPANESE_DOUBLE_POSTCARD.getValue() == 49, "JAPANESE_DOUBLE_POSTCARD value == 49");
        check("oufuko-postcard".equals(MediaSizeName.JAPANESE_DOUBLE_POSTCARD.toString()), "JAPANESE_DOUBLE_POSTCARD toString");
        check(MediaSizeName.A.getValue() == 50, "A value == 50");
        check("a".equals(MediaSizeName.A.toString()), "A toString");
        check(MediaSizeName.B.getValue() == 51, "B value == 51");
        check("b".equals(MediaSizeName.B.toString()), "B toString");
        check(MediaSizeName.C.getValue() == 52, "C value == 52");
        check("c".equals(MediaSizeName.C.toString()), "C toString");
        check(MediaSizeName.D.getValue() == 53, "D value == 53");
        check("d".equals(MediaSizeName.D.toString()), "D toString");
        check(MediaSizeName.E.getValue() == 54, "E value == 54");
        check("e".equals(MediaSizeName.E.toString()), "E toString");
        check(MediaSizeName.ISO_DESIGNATED_LONG.getValue() == 55, "ISO_DESIGNATED_LONG value == 55");
        check("iso-designated-long".equals(MediaSizeName.ISO_DESIGNATED_LONG.toString()), "ISO_DESIGNATED_LONG toString");
        check(MediaSizeName.ITALY_ENVELOPE.getValue() == 56, "ITALY_ENVELOPE value == 56");
        check("italian-envelope".equals(MediaSizeName.ITALY_ENVELOPE.toString()), "ITALY_ENVELOPE toString");
        check(MediaSizeName.MONARCH_ENVELOPE.getValue() == 57, "MONARCH_ENVELOPE value == 57");
        check("monarch-envelope".equals(MediaSizeName.MONARCH_ENVELOPE.toString()), "MONARCH_ENVELOPE toString");
        check(MediaSizeName.PERSONAL_ENVELOPE.getValue() == 58, "PERSONAL_ENVELOPE value == 58");
        check("personal-envelope".equals(MediaSizeName.PERSONAL_ENVELOPE.toString()), "PERSONAL_ENVELOPE toString");
        check(MediaSizeName.NA_NUMBER_9_ENVELOPE.getValue() == 59, "NA_NUMBER_9_ENVELOPE value == 59");
        check("na-number-9-envelope".equals(MediaSizeName.NA_NUMBER_9_ENVELOPE.toString()), "NA_NUMBER_9_ENVELOPE toString");
        check(MediaSizeName.NA_NUMBER_10_ENVELOPE.getValue() == 60, "NA_NUMBER_10_ENVELOPE value == 60");
        check("na-number-10-envelope".equals(MediaSizeName.NA_NUMBER_10_ENVELOPE.toString()), "NA_NUMBER_10_ENVELOPE toString");
        check(MediaSizeName.NA_NUMBER_11_ENVELOPE.getValue() == 61, "NA_NUMBER_11_ENVELOPE value == 61");
        check("na-number-11-envelope".equals(MediaSizeName.NA_NUMBER_11_ENVELOPE.toString()), "NA_NUMBER_11_ENVELOPE toString");
        check(MediaSizeName.NA_NUMBER_12_ENVELOPE.getValue() == 62, "NA_NUMBER_12_ENVELOPE value == 62");
        check("na-number-12-envelope".equals(MediaSizeName.NA_NUMBER_12_ENVELOPE.toString()), "NA_NUMBER_12_ENVELOPE toString");
        check(MediaSizeName.NA_NUMBER_14_ENVELOPE.getValue() == 63, "NA_NUMBER_14_ENVELOPE value == 63");
        check("na-number-14-envelope".equals(MediaSizeName.NA_NUMBER_14_ENVELOPE.toString()), "NA_NUMBER_14_ENVELOPE toString");
        check(MediaSizeName.NA_6X9_ENVELOPE.getValue() == 64, "NA_6X9_ENVELOPE value == 64");
        check("na-6x9-envelope".equals(MediaSizeName.NA_6X9_ENVELOPE.toString()), "NA_6X9_ENVELOPE toString");
        check(MediaSizeName.NA_7X9_ENVELOPE.getValue() == 65, "NA_7X9_ENVELOPE value == 65");
        check("na-7x9-envelope".equals(MediaSizeName.NA_7X9_ENVELOPE.toString()), "NA_7X9_ENVELOPE toString");
        check(MediaSizeName.NA_9X11_ENVELOPE.getValue() == 66, "NA_9X11_ENVELOPE value == 66");
        check("na-9x11-envelope".equals(MediaSizeName.NA_9X11_ENVELOPE.toString()), "NA_9X11_ENVELOPE toString");
        check(MediaSizeName.NA_9X12_ENVELOPE.getValue() == 67, "NA_9X12_ENVELOPE value == 67");
        check("na-9x12-envelope".equals(MediaSizeName.NA_9X12_ENVELOPE.toString()), "NA_9X12_ENVELOPE toString");
        check(MediaSizeName.NA_10X13_ENVELOPE.getValue() == 68, "NA_10X13_ENVELOPE value == 68");
        check("na-10x13-envelope".equals(MediaSizeName.NA_10X13_ENVELOPE.toString()), "NA_10X13_ENVELOPE toString");
        check(MediaSizeName.NA_10X14_ENVELOPE.getValue() == 69, "NA_10X14_ENVELOPE value == 69");
        check("na-10x14-envelope".equals(MediaSizeName.NA_10X14_ENVELOPE.toString()), "NA_10X14_ENVELOPE toString");
        check(MediaSizeName.NA_10X15_ENVELOPE.getValue() == 70, "NA_10X15_ENVELOPE value == 70");
        check("na-10x15-envelope".equals(MediaSizeName.NA_10X15_ENVELOPE.toString()), "NA_10X15_ENVELOPE toString");
        check(MediaSizeName.NA_5X7.getValue() == 71, "NA_5X7 value == 71");
        check("na-5x7".equals(MediaSizeName.NA_5X7.toString()), "NA_5X7 toString");
        check(MediaSizeName.NA_8X10.getValue() == 72, "NA_8X10 value == 72");
        check("na-8x10".equals(MediaSizeName.NA_8X10.toString()), "NA_8X10 toString");
        check(MediaSizeName.ISO_A0.clone() == MediaSizeName.ISO_A0, "clone returns singleton");
        check(MediaSizeName.ISO_A0.hashCode() == 0, "hashCode == value");
        check(MediaSizeName.ISO_A0.equals(MediaSizeName.ISO_A0), "equals self");
        check(!MediaSizeName.ISO_A0.equals(MediaSizeName.ISO_A1), "distinct constants unequal");
        check(!MediaSizeName.ISO_A0.equals(null) && !MediaSizeName.ISO_A0.equals("x"), "not equals null/foreign");

        // direct protected-method invocation through a minimal subclass (super constructor + super getStringTable/getEnumValueTable)
        MSN m = new MSN(0);
        String[] st = m.stringTable();
        check(st != null && st.length == 73, "getStringTable length");
        check("iso-a0".equals(st[0]), "getStringTable first entry");
        javax.print.attribute.EnumSyntax[] evt = m.enumValueTable();
        check(evt != null && evt.length == 73, "getEnumValueTable length");
        check(evt[0] == MediaSizeName.ISO_A0, "getEnumValueTable first entry");
        check(m.getValue() == 0 && "iso-a0".equals(m.toString()), "subclass ctor sets value 0");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: value table / name mapping / equality with MediaSize ----
    static void testContractDeep() throws Exception {
        check(MediaSizeName.ISO_A4.getValue() == 4, "ISO_A4 value 4");
        check(MediaSizeName.NA_LETTER.getValue() == 40, "NA_LETTER value 40");
        check(MediaSizeName.JIS_B5.getValue() == 27, "JIS_B5 value 27");
        check("iso-a4".equals(MediaSizeName.ISO_A4.toString()), "ISO_A4 toString");
        check("na-letter".equals(MediaSizeName.NA_LETTER.toString()), "NA_LETTER toString");
        check("jis-b5".equals(MediaSizeName.JIS_B5.toString()), "JIS_B5 toString");
        javax.print.attribute.standard.MediaSize sz = MediaSize.ISO.A4;
        check(sz.getMediaSizeName() == MediaSizeName.ISO_A4, "A4 size links name");
        check(MediaSizeName.class == MediaSizeName.ISO_A4.getCategory() || javax.print.attribute.standard.Media.class == MediaSizeName.ISO_A4.getCategory(), "category is Media");
        check("media".equals(MediaSizeName.ISO_A4.getName()), "name media");
        check(serRoundTrip(MediaSizeName.ISO_A3) == MediaSizeName.ISO_A3, "serialization singleton");
        check(MediaSizeName.ISO_A4.equals(MediaSizeName.ISO_A4), "equals self");
        check(!MediaSizeName.ISO_A4.equals(MediaSizeName.ISO_A5), "different unequal");
        check(MediaSizeName.ISO_A4.hashCode() == 4, "hashCode == value");
        MediaSizeName[] all = { MediaSizeName.ISO_A0, MediaSizeName.ISO_A10, MediaSizeName.NA_NUMBER_14_ENVELOPE };
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : all) vals.add(e.getValue());
        check(vals.size() == all.length, "distinct values");
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
