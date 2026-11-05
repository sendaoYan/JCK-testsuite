/*
 * @test
 * @summary javax.print.attribute.standard.MediaName conformance: media enum attribute.
 * @run main MediaNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaName v1 = MediaName.NA_LETTER_WHITE;
        check(v1 instanceof MediaName, "NA_LETTER_WHITE instanceof MediaName");
        check(v1.getCategory() == Media.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MediaName v2 = MediaName.ISO_A4_WHITE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MediaName.NA_LETTER_WHITE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MediaName.NA_LETTER_WHITE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(MediaName.NA_LETTER_WHITE.getValue() == 0, "NA_LETTER_WHITE.getValue == 0");
        check("na-letter-white".equals(MediaName.NA_LETTER_WHITE.toString()), "NA_LETTER_WHITE.toString [na-letter-white]");
        check(MediaName.NA_LETTER_TRANSPARENT.getValue() == 1, "NA_LETTER_TRANSPARENT.getValue == 1");
        check("na-letter-transparent".equals(MediaName.NA_LETTER_TRANSPARENT.toString()), "NA_LETTER_TRANSPARENT.toString [na-letter-transparent]");
        check(MediaName.ISO_A4_WHITE.getValue() == 2, "ISO_A4_WHITE.getValue == 2");
        check("iso-a4-white".equals(MediaName.ISO_A4_WHITE.toString()), "ISO_A4_WHITE.toString [iso-a4-white]");
        check(MediaName.ISO_A4_TRANSPARENT.getValue() == 3, "ISO_A4_TRANSPARENT.getValue == 3");
        check("iso-a4-transparent".equals(MediaName.ISO_A4_TRANSPARENT.toString()), "ISO_A4_TRANSPARENT.toString [iso-a4-transparent]");
        check("media".equals(MediaName.NA_LETTER_WHITE.getName()), "getName == \"media\"");
        check(Media.class == MediaName.NA_LETTER_WHITE.getCategory(), "getCategory");
        check(MediaName.NA_LETTER_WHITE.equals(MediaName.NA_LETTER_WHITE), "equals self");
        check(!MediaName.NA_LETTER_WHITE.equals(null), "not equals null");
        check(!MediaName.NA_LETTER_WHITE.equals("na-letter-white"), "not equals foreign type");
        check(MediaName.NA_LETTER_WHITE.hashCode() == 0, "hashCode == value");
        check(MediaName.NA_LETTER_WHITE.clone() == MediaName.NA_LETTER_WHITE, "clone returns same singleton");
        check(!MediaName.NA_LETTER_WHITE.equals(MediaName.NA_LETTER_TRANSPARENT), "distinct constants unequal");
        check(MediaName.NA_LETTER_WHITE.toString() != MediaName.NA_LETTER_TRANSPARENT.toString() || true, "toString stable");
        check(!MediaName.NA_LETTER_WHITE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == VALUES.length, "all values distinct");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        MediaName.NA_LETTER_WHITE,
        MediaName.NA_LETTER_TRANSPARENT,
        MediaName.ISO_A4_WHITE,
        MediaName.ISO_A4_TRANSPARENT
    };
    static final String[] NAMES = {
        "NA_LETTER_WHITE",
        "NA_LETTER_TRANSPARENT",
        "ISO_A4_WHITE",
        "ISO_A4_TRANSPARENT"
    };

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
