/*
 * @test
 * @summary javax.xml.catalog.CatalogFeatures.Feature conformance: white-box structure + black-box behavior.
 * @run main FeatureTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class FeatureTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.catalog.CatalogFeatures$Feature");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "superclass Enum");
        check(hasMethod(c, "defaultValue"), "method defaultValue");
        check(hasMethod(c, "getPropertyName"), "method getPropertyName");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "DEFER"), "field DEFER");
        check(hasField(c, "FILES"), "field FILES");
        check(hasField(c, "PREFER"), "field PREFER");
        check(hasField(c, "RESOLVE"), "field RESOLVE");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 4, "4 enum constants");
        check(hasField(c, "DEFER"), "enum constant DEFER");
        check(hasField(c, "FILES"), "enum constant FILES");
        check(hasField(c, "PREFER"), "enum constant PREFER");
        check(hasField(c, "RESOLVE"), "enum constant RESOLVE");

        javax.xml.catalog.CatalogFeatures.Feature f = javax.xml.catalog.CatalogFeatures.Feature.PREFER;
        check(f.getPropertyName() != null, "getPropertyName");
        check("public".equals(f.defaultValue()), "defaultValue");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check("javax.xml.catalog.files".equals(javax.xml.catalog.CatalogFeatures.Feature.FILES.getPropertyName()), "FILES property name");
        check("javax.xml.catalog.prefer".equals(javax.xml.catalog.CatalogFeatures.Feature.PREFER.getPropertyName()), "PREFER property name");
        check("javax.xml.catalog.defer".equals(javax.xml.catalog.CatalogFeatures.Feature.DEFER.getPropertyName()), "DEFER property name");
        check("javax.xml.catalog.resolve".equals(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE.getPropertyName()), "RESOLVE property name");
        check(javax.xml.catalog.CatalogFeatures.Feature.valueOf("PREFER") == javax.xml.catalog.CatalogFeatures.Feature.PREFER, "valueOf");
        check(javax.xml.catalog.CatalogFeatures.Feature.values().length == 4, "four features");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (enum surface + per-feature defaults) ----
    static void testEnumSurface() {
        javax.xml.catalog.CatalogFeatures.Feature[] vals = javax.xml.catalog.CatalogFeatures.Feature.values();
        check(vals.length == 4, "exactly four features");
        // canonical order verified against javap: FILES, PREFER, DEFER, RESOLVE
        check(vals[0] == javax.xml.catalog.CatalogFeatures.Feature.FILES
              && vals[1] == javax.xml.catalog.CatalogFeatures.Feature.PREFER
              && vals[2] == javax.xml.catalog.CatalogFeatures.Feature.DEFER
              && vals[3] == javax.xml.catalog.CatalogFeatures.Feature.RESOLVE,
              "declaration order FILES, PREFER, DEFER, RESOLVE");
        // system property names (authoritative, javap-verified)
        check("javax.xml.catalog.files".equals(vals[0].getPropertyName()), "FILES system property name");
        check("javax.xml.catalog.prefer".equals(vals[1].getPropertyName()), "PREFER system property name");
        check("javax.xml.catalog.defer".equals(vals[2].getPropertyName()), "DEFER system property name");
        check("javax.xml.catalog.resolve".equals(vals[3].getPropertyName()), "RESOLVE system property name");
        // default values: FILES unset (null); others fixed strings
        check(vals[0].defaultValue() == null, "FILES defaultValue null");
        check("public".equals(vals[1].defaultValue()), "PREFER defaultValue 'public'");
        check("true".equals(vals[2].defaultValue()), "DEFER defaultValue 'true'");
        check("strict".equals(vals[3].defaultValue()), "RESOLVE defaultValue 'strict'");
        // valueOf round-trips every constant
        for (javax.xml.catalog.CatalogFeatures.Feature f : vals)
            check(javax.xml.catalog.CatalogFeatures.Feature.valueOf(f.name()) == f,
                  "valueOf('" + f.name() + "') round-trip");
        // unknown name -> IAE
        boolean iae = false;
        try { javax.xml.catalog.CatalogFeatures.Feature.valueOf("bogus"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "valueOf('bogus') -> IllegalArgumentException");
        // enum toString is the constant name
        for (javax.xml.catalog.CatalogFeatures.Feature f : vals)
            check(f.name().equals(f.toString()), f + " toString == name");
    }
}
