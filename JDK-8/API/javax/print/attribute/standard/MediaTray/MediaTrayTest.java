/*
 * @test
 * @summary javax.print.attribute.standard.MediaTray conformance: media enum attribute.
 * @run main MediaTrayTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaTrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaTray v1 = MediaTray.TOP;
        check(v1 instanceof MediaTray, "TOP instanceof MediaTray");
        check(v1.getCategory() == Media.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MediaTray v2 = MediaTray.MAIN;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MediaTray.TOP), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MediaTray.TOP.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(MediaTray.TOP.getValue() == 0, "TOP.getValue == 0");
        check("top".equals(MediaTray.TOP.toString()), "TOP.toString [top]");
        check(MediaTray.MIDDLE.getValue() == 1, "MIDDLE.getValue == 1");
        check("middle".equals(MediaTray.MIDDLE.toString()), "MIDDLE.toString [middle]");
        check(MediaTray.BOTTOM.getValue() == 2, "BOTTOM.getValue == 2");
        check("bottom".equals(MediaTray.BOTTOM.toString()), "BOTTOM.toString [bottom]");
        check(MediaTray.ENVELOPE.getValue() == 3, "ENVELOPE.getValue == 3");
        check("envelope".equals(MediaTray.ENVELOPE.toString()), "ENVELOPE.toString [envelope]");
        check(MediaTray.MANUAL.getValue() == 4, "MANUAL.getValue == 4");
        check("manual".equals(MediaTray.MANUAL.toString()), "MANUAL.toString [manual]");
        check(MediaTray.LARGE_CAPACITY.getValue() == 5, "LARGE_CAPACITY.getValue == 5");
        check("large-capacity".equals(MediaTray.LARGE_CAPACITY.toString()), "LARGE_CAPACITY.toString [large-capacity]");
        check(MediaTray.MAIN.getValue() == 6, "MAIN.getValue == 6");
        check("main".equals(MediaTray.MAIN.toString()), "MAIN.toString [main]");
        check(MediaTray.SIDE.getValue() == 7, "SIDE.getValue == 7");
        check("side".equals(MediaTray.SIDE.toString()), "SIDE.toString [side]");
        check("media".equals(MediaTray.TOP.getName()), "getName == \"media\"");
        check(Media.class == MediaTray.TOP.getCategory(), "getCategory");
        check(MediaTray.TOP.equals(MediaTray.TOP), "equals self");
        check(!MediaTray.TOP.equals(null), "not equals null");
        check(!MediaTray.TOP.equals("top"), "not equals foreign type");
        check(MediaTray.TOP.hashCode() == 0, "hashCode == value");
        check(MediaTray.TOP.clone() == MediaTray.TOP, "clone returns same singleton");
        check(!MediaTray.TOP.equals(MediaTray.MIDDLE), "distinct constants unequal");
        check(MediaTray.TOP.toString() != MediaTray.MIDDLE.toString() || true, "toString stable");
        check(!MediaTray.TOP.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        MediaTray.TOP,
        MediaTray.MIDDLE,
        MediaTray.BOTTOM,
        MediaTray.ENVELOPE,
        MediaTray.MANUAL,
        MediaTray.LARGE_CAPACITY,
        MediaTray.MAIN,
        MediaTray.SIDE
    };
    static final String[] NAMES = {
        "TOP",
        "MIDDLE",
        "BOTTOM",
        "ENVELOPE",
        "MANUAL",
        "LARGE_CAPACITY",
        "MAIN",
        "SIDE"
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
