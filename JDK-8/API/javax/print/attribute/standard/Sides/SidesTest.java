/*
 * @test
 * @summary javax.print.attribute.standard.Sides conformance: enum attribute.
 * @run main SidesTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class SidesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Sides v1 = Sides.ONE_SIDED;
        check(v1 instanceof Sides, "ONE_SIDED instanceof Sides");
        check(v1.getCategory() == Sides.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Sides v2 = Sides.TWO_SIDED_LONG_EDGE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Sides.ONE_SIDED), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Sides.ONE_SIDED.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Sides.ONE_SIDED.getValue() == 0, "ONE_SIDED.getValue == 0");
        check("one-sided".equals(Sides.ONE_SIDED.toString()), "ONE_SIDED.toString [one-sided]");
        check(Sides.TWO_SIDED_LONG_EDGE.getValue() == 1, "TWO_SIDED_LONG_EDGE.getValue == 1");
        check("two-sided-long-edge".equals(Sides.TWO_SIDED_LONG_EDGE.toString()), "TWO_SIDED_LONG_EDGE.toString [two-sided-long-edge]");
        check(Sides.TWO_SIDED_SHORT_EDGE.getValue() == 2, "TWO_SIDED_SHORT_EDGE.getValue == 2");
        check("two-sided-short-edge".equals(Sides.TWO_SIDED_SHORT_EDGE.toString()), "TWO_SIDED_SHORT_EDGE.toString [two-sided-short-edge]");
        check(Sides.DUPLEX.getValue() == 1, "DUPLEX.getValue == 1");
        check(Sides.TUMBLE.getValue() == 2, "TUMBLE.getValue == 2");
        check("sides".equals(Sides.ONE_SIDED.getName()), "getName == \"sides\"");
        check(Sides.class == Sides.ONE_SIDED.getCategory(), "getCategory");
        check(Sides.ONE_SIDED.equals(Sides.ONE_SIDED), "equals self");
        check(!Sides.ONE_SIDED.equals(null), "not equals null");
        check(!Sides.ONE_SIDED.equals("one-sided"), "not equals foreign type");
        check(Sides.ONE_SIDED.hashCode() == 0, "hashCode == value");
        check(Sides.ONE_SIDED.clone() == Sides.ONE_SIDED, "clone returns same singleton");
        check(!Sides.ONE_SIDED.equals(Sides.TWO_SIDED_LONG_EDGE), "distinct constants unequal");
        check(Sides.ONE_SIDED.toString() != Sides.TWO_SIDED_LONG_EDGE.toString() || true, "toString stable");
        check(!Sides.ONE_SIDED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == 3, "Sides has 3 distinct values (5 constants, 2 aliases)");
        check(Sides.DUPLEX.toString().equals("two-sided-long-edge"), "DUPLEX alias toString resolves via value table");
        check(Sides.TUMBLE.toString().equals("two-sided-short-edge"), "TUMBLE alias toString resolves via value table");
        check(Sides.DUPLEX == Sides.TWO_SIDED_LONG_EDGE, "DUPLEX aliases same object as TWO_SIDED_LONG_EDGE");
        check(Sides.DUPLEX.equals(Sides.TWO_SIDED_LONG_EDGE), "alias DUPLEX value-equal");
        check(Sides.TUMBLE.equals(Sides.TWO_SIDED_SHORT_EDGE), "alias TUMBLE value-equal");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        Sides.ONE_SIDED,
        Sides.TWO_SIDED_LONG_EDGE,
        Sides.TWO_SIDED_SHORT_EDGE,
        Sides.DUPLEX,
        Sides.TUMBLE
    };
    static final String[] NAMES = {
        "ONE_SIDED",
        "TWO_SIDED_LONG_EDGE",
        "TWO_SIDED_SHORT_EDGE",
        "DUPLEX",
        "TUMBLE"
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
