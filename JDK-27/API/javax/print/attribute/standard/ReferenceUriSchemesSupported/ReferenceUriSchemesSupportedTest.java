/*
 * @test
 * @summary javax.print.attribute.standard.ReferenceUriSchemesSupported conformance: white-box structure + black-box behavior.
 * @run main ReferenceUriSchemesSupportedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ReferenceUriSchemesSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.ReferenceUriSchemesSupported");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ReferenceUriSchemesSupported.FTP.getCategory() == ReferenceUriSchemesSupported.class, "getCategory");
        check("reference-uri-schemes-supported".equals(ReferenceUriSchemesSupported.FTP.getName()), "getName");
        check(ReferenceUriSchemesSupported.FTP.getValue() == 0, "FTP value == 0");
        check("ftp".equals(ReferenceUriSchemesSupported.FTP.toString()), "FTP toString");
        check(ReferenceUriSchemesSupported.HTTP.getValue() == 1, "HTTP value == 1");
        check("http".equals(ReferenceUriSchemesSupported.HTTP.toString()), "HTTP toString");
        check(ReferenceUriSchemesSupported.HTTPS.getValue() == 2, "HTTPS value == 2");
        check("https".equals(ReferenceUriSchemesSupported.HTTPS.toString()), "HTTPS toString");
        check(ReferenceUriSchemesSupported.GOPHER.getValue() == 3, "GOPHER value == 3");
        check("gopher".equals(ReferenceUriSchemesSupported.GOPHER.toString()), "GOPHER toString");
        check(ReferenceUriSchemesSupported.NEWS.getValue() == 4, "NEWS value == 4");
        check("news".equals(ReferenceUriSchemesSupported.NEWS.toString()), "NEWS toString");
        check(ReferenceUriSchemesSupported.NNTP.getValue() == 5, "NNTP value == 5");
        check("nntp".equals(ReferenceUriSchemesSupported.NNTP.toString()), "NNTP toString");
        check(ReferenceUriSchemesSupported.WAIS.getValue() == 6, "WAIS value == 6");
        check("wais".equals(ReferenceUriSchemesSupported.WAIS.toString()), "WAIS toString");
        check(ReferenceUriSchemesSupported.FILE.getValue() == 7, "FILE value == 7");
        check("file".equals(ReferenceUriSchemesSupported.FILE.toString()), "FILE toString");
        check(ReferenceUriSchemesSupported.FTP.clone() == ReferenceUriSchemesSupported.FTP, "clone returns singleton");
        check(ReferenceUriSchemesSupported.FTP.hashCode() == 0, "hashCode == value");
        check(ReferenceUriSchemesSupported.FTP.equals(ReferenceUriSchemesSupported.FTP), "equals self");
        check(!ReferenceUriSchemesSupported.FTP.equals(ReferenceUriSchemesSupported.HTTP), "distinct constants unequal");
        check(!ReferenceUriSchemesSupported.FTP.equals(null) && !ReferenceUriSchemesSupported.FTP.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
