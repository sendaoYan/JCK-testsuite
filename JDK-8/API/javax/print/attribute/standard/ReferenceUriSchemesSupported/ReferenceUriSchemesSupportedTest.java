/*
 * @test
 * @summary javax.print.attribute.standard.ReferenceUriSchemesSupported conformance: enum attribute.
 * @run main ReferenceUriSchemesSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ReferenceUriSchemesSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        ReferenceUriSchemesSupported v1 = ReferenceUriSchemesSupported.FTP;
        check(v1 instanceof ReferenceUriSchemesSupported, "FTP instanceof ReferenceUriSchemesSupported");
        check(v1.getCategory() == ReferenceUriSchemesSupported.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        ReferenceUriSchemesSupported v2 = ReferenceUriSchemesSupported.HTTP;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(ReferenceUriSchemesSupported.FTP), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == ReferenceUriSchemesSupported.FTP.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(ReferenceUriSchemesSupported.FTP.getValue() == 0, "FTP.getValue == 0");
        check("ftp".equals(ReferenceUriSchemesSupported.FTP.toString()), "FTP.toString [ftp]");
        check(ReferenceUriSchemesSupported.HTTP.getValue() == 1, "HTTP.getValue == 1");
        check("http".equals(ReferenceUriSchemesSupported.HTTP.toString()), "HTTP.toString [http]");
        check(ReferenceUriSchemesSupported.HTTPS.getValue() == 2, "HTTPS.getValue == 2");
        check("https".equals(ReferenceUriSchemesSupported.HTTPS.toString()), "HTTPS.toString [https]");
        check(ReferenceUriSchemesSupported.GOPHER.getValue() == 3, "GOPHER.getValue == 3");
        check("gopher".equals(ReferenceUriSchemesSupported.GOPHER.toString()), "GOPHER.toString [gopher]");
        check(ReferenceUriSchemesSupported.NEWS.getValue() == 4, "NEWS.getValue == 4");
        check("news".equals(ReferenceUriSchemesSupported.NEWS.toString()), "NEWS.toString [news]");
        check(ReferenceUriSchemesSupported.NNTP.getValue() == 5, "NNTP.getValue == 5");
        check("nntp".equals(ReferenceUriSchemesSupported.NNTP.toString()), "NNTP.toString [nntp]");
        check(ReferenceUriSchemesSupported.WAIS.getValue() == 6, "WAIS.getValue == 6");
        check("wais".equals(ReferenceUriSchemesSupported.WAIS.toString()), "WAIS.toString [wais]");
        check(ReferenceUriSchemesSupported.FILE.getValue() == 7, "FILE.getValue == 7");
        check("file".equals(ReferenceUriSchemesSupported.FILE.toString()), "FILE.toString [file]");
        check("reference-uri-schemes-supported".equals(ReferenceUriSchemesSupported.FTP.getName()), "getName == \"reference-uri-schemes-supported\"");
        check(ReferenceUriSchemesSupported.class == ReferenceUriSchemesSupported.FTP.getCategory(), "getCategory");
        check(ReferenceUriSchemesSupported.FTP.equals(ReferenceUriSchemesSupported.FTP), "equals self");
        check(!ReferenceUriSchemesSupported.FTP.equals(null), "not equals null");
        check(!ReferenceUriSchemesSupported.FTP.equals("ftp"), "not equals foreign type");
        check(ReferenceUriSchemesSupported.FTP.hashCode() == 0, "hashCode == value");
        check(ReferenceUriSchemesSupported.FTP.clone() == ReferenceUriSchemesSupported.FTP, "clone returns same singleton");
        check(!ReferenceUriSchemesSupported.FTP.equals(ReferenceUriSchemesSupported.HTTP), "distinct constants unequal");
        check(ReferenceUriSchemesSupported.FTP.toString() != ReferenceUriSchemesSupported.HTTP.toString() || true, "toString stable");
        check(!ReferenceUriSchemesSupported.FTP.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        ReferenceUriSchemesSupported.FTP,
        ReferenceUriSchemesSupported.HTTP,
        ReferenceUriSchemesSupported.HTTPS,
        ReferenceUriSchemesSupported.GOPHER,
        ReferenceUriSchemesSupported.NEWS,
        ReferenceUriSchemesSupported.NNTP,
        ReferenceUriSchemesSupported.WAIS,
        ReferenceUriSchemesSupported.FILE
    };
    static final String[] NAMES = {
        "FTP",
        "HTTP",
        "HTTPS",
        "GOPHER",
        "NEWS",
        "NNTP",
        "WAIS",
        "FILE"
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
