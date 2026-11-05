/*
 * @test
 * @summary javax.print.attribute.standard.MediaSizeName conformance: media enum attribute.
 * @run main MediaSizeNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaSizeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaSizeName v1 = MediaSizeName.ISO_A4;
        check(v1 instanceof MediaSizeName, "ISO_A4 instanceof MediaSizeName");
        check(v1.getCategory() == Media.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MediaSizeName v2 = MediaSizeName.NA_LETTER;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MediaSizeName.ISO_A4), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MediaSizeName.ISO_A4.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
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
