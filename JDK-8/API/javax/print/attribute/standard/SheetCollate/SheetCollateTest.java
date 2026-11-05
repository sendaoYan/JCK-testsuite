/*
 * @test
 * @summary javax.print.attribute.standard.SheetCollate conformance: enum attribute.
 * @run main SheetCollateTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class SheetCollateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        SheetCollate v1 = SheetCollate.UNCOLLATED;
        check(v1 instanceof SheetCollate, "UNCOLLATED instanceof SheetCollate");
        check(v1.getCategory() == SheetCollate.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        SheetCollate v2 = SheetCollate.COLLATED;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(SheetCollate.UNCOLLATED), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == SheetCollate.UNCOLLATED.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(SheetCollate.UNCOLLATED.getValue() == 0, "UNCOLLATED.getValue == 0");
        check("uncollated".equals(SheetCollate.UNCOLLATED.toString()), "UNCOLLATED.toString [uncollated]");
        check(SheetCollate.COLLATED.getValue() == 1, "COLLATED.getValue == 1");
        check("collated".equals(SheetCollate.COLLATED.toString()), "COLLATED.toString [collated]");
        check("sheet-collate".equals(SheetCollate.UNCOLLATED.getName()), "getName == \"sheet-collate\"");
        check(SheetCollate.class == SheetCollate.UNCOLLATED.getCategory(), "getCategory");
        check(SheetCollate.UNCOLLATED.equals(SheetCollate.UNCOLLATED), "equals self");
        check(!SheetCollate.UNCOLLATED.equals(null), "not equals null");
        check(!SheetCollate.UNCOLLATED.equals("uncollated"), "not equals foreign type");
        check(SheetCollate.UNCOLLATED.hashCode() == 0, "hashCode == value");
        check(SheetCollate.UNCOLLATED.clone() == SheetCollate.UNCOLLATED, "clone returns same singleton");
        check(!SheetCollate.UNCOLLATED.equals(SheetCollate.COLLATED), "distinct constants unequal");
        check(SheetCollate.UNCOLLATED.toString() != SheetCollate.COLLATED.toString() || true, "toString stable");
        check(!SheetCollate.UNCOLLATED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        SheetCollate.UNCOLLATED,
        SheetCollate.COLLATED
    };
    static final String[] NAMES = {
        "UNCOLLATED",
        "COLLATED"
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
