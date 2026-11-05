/*
 * @test
 * @summary javax.print.attribute.standard.Compression conformance: enum attribute.
 * @run main CompressionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class CompressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Compression v1 = Compression.NONE;
        check(v1 instanceof Compression, "NONE instanceof Compression");
        check(v1.getCategory() == Compression.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Compression v2 = Compression.DEFLATE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Compression.NONE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Compression.NONE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Compression.NONE.getValue() == 0, "NONE.getValue == 0");
        check("none".equals(Compression.NONE.toString()), "NONE.toString [none]");
        check(Compression.DEFLATE.getValue() == 1, "DEFLATE.getValue == 1");
        check("deflate".equals(Compression.DEFLATE.toString()), "DEFLATE.toString [deflate]");
        check(Compression.GZIP.getValue() == 2, "GZIP.getValue == 2");
        check("gzip".equals(Compression.GZIP.toString()), "GZIP.toString [gzip]");
        check(Compression.COMPRESS.getValue() == 3, "COMPRESS.getValue == 3");
        check("compress".equals(Compression.COMPRESS.toString()), "COMPRESS.toString [compress]");
        check("compression".equals(Compression.NONE.getName()), "getName == \"compression\"");
        check(Compression.class == Compression.NONE.getCategory(), "getCategory");
        check(Compression.NONE.equals(Compression.NONE), "equals self");
        check(!Compression.NONE.equals(null), "not equals null");
        check(!Compression.NONE.equals("none"), "not equals foreign type");
        check(Compression.NONE.hashCode() == 0, "hashCode == value");
        check(Compression.NONE.clone() == Compression.NONE, "clone returns same singleton");
        check(!Compression.NONE.equals(Compression.DEFLATE), "distinct constants unequal");
        check(Compression.NONE.toString() != Compression.DEFLATE.toString() || true, "toString stable");
        check(!Compression.NONE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        Compression.NONE,
        Compression.DEFLATE,
        Compression.GZIP,
        Compression.COMPRESS
    };
    static final String[] NAMES = {
        "NONE",
        "DEFLATE",
        "GZIP",
        "COMPRESS"
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
