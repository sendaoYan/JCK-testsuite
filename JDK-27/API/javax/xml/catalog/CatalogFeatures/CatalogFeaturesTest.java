/*
 * @test
 * @summary javax.xml.catalog.CatalogFeatures conformance: white-box structure + black-box behavior.
 * @run main CatalogFeaturesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogFeaturesTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.CatalogFeatures");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "builder"), "method builder");
        check(hasMethod(c, "defaults"), "method defaults");
        check(hasMethod(c, "get"), "method get");

        javax.xml.catalog.CatalogFeatures f = javax.xml.catalog.CatalogFeatures.defaults();
        check(f != null, "defaults");
        javax.xml.catalog.CatalogFeatures b = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system").build();
        check(b != null, "builder build");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.defaults();
        check(cf != null, "defaults");
        check("public".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "default PREFER");
        check("strict".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "default RESOLVE");
        check("true".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER)), "default DEFER");
        javax.xml.catalog.CatalogFeatures.Builder b = javax.xml.catalog.CatalogFeatures.builder();
        javax.xml.catalog.CatalogFeatures cf2 = b.with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system").build();
        check("system".equals(cf2.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "builder with PREFER");
        check("strict".equals(cf2.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "builder inherits default RESOLVE");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDefaultsMatrix();
        testBuilderValidation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (defaults + builder validation) ----
    static void testDefaultsMatrix() {
        javax.xml.catalog.CatalogFeatures d = javax.xml.catalog.CatalogFeatures.defaults();
        check(d.get(javax.xml.catalog.CatalogFeatures.Feature.FILES) == null,
              "default FILES is null (unset)");
        check("public".equals(d.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "default PREFER 'public'");
        check("true".equals(d.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER)), "default DEFER 'true'");
        check("strict".equals(d.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "default RESOLVE 'strict'");
        // defaults() is a stable singleton-like accessor (non-null every call)
        check(javax.xml.catalog.CatalogFeatures.defaults() != null, "defaults() repeat call non-null");
        // per-Feature defaults agree with the defaults() view
        for (javax.xml.catalog.CatalogFeatures.Feature f : javax.xml.catalog.CatalogFeatures.Feature.values()) {
            String dv = f.defaultValue();
            String gv = d.get(f);
            check(java.util.Objects.equals(dv, gv), "defaultValue(" + f + ") == defaults().get(" + f + ")");
        }
        // get(null) -> NPE
        boolean npe = false;
        try { d.get(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "get(null Feature) -> NullPointerException");
        // CatalogFeatures has no value-based equals: two empty builds are NOT equal (recorded)
        check(!javax.xml.catalog.CatalogFeatures.builder().build()
              .equals(javax.xml.catalog.CatalogFeatures.builder().build()),
              "builder().build() twice not equals (no value semantics, recorded)");
        check(!d.equals(javax.xml.catalog.CatalogFeatures.builder().build()),
              "defaults() != builder().build() (FILES state differs, recorded)");
    }

    static void testBuilderValidation() {
        // FILES round-trip with an absolute URI
        javax.xml.catalog.CatalogFeatures withFiles = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.FILES, "file:///tmp/jcct-catalog.xml").build();
        check("file:///tmp/jcct-catalog.xml".equals(withFiles.get(javax.xml.catalog.CatalogFeatures.Feature.FILES)),
              "FILES round-trip");
        // last write wins for a repeated feature
        javax.xml.catalog.CatalogFeatures override = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "public")
            .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system").build();
        check("system".equals(override.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)),
              "repeated with() keeps the last value");
        // unspecified features inherit defaults
        javax.xml.catalog.CatalogFeatures partial = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system").build();
        check("strict".equals(partial.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE))
              && "true".equals(partial.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER)),
              "unspecified features inherit defaults");

        // with(null feature) -> NPE
        boolean npe = false;
        try { javax.xml.catalog.CatalogFeatures.builder().with(null, "x"); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "with(null Feature) -> NullPointerException");

        // invalid values -> IAE thrown by with(), message cites value and feature
        boolean[][] seenIae = new boolean[4][1];
        String[] msgs = new String[4];
        javax.xml.catalog.CatalogFeatures.Feature[] feats = {
            javax.xml.catalog.CatalogFeatures.Feature.PREFER,
            javax.xml.catalog.CatalogFeatures.Feature.DEFER,
            javax.xml.catalog.CatalogFeatures.Feature.RESOLVE,
            javax.xml.catalog.CatalogFeatures.Feature.FILES,
        };
        String[] values = {"bogus", "bogus", "bogus", "  "};
        for (int i = 0; i < feats.length; i++) {
            try { javax.xml.catalog.CatalogFeatures.builder().with(feats[i], values[i]); }
            catch (IllegalArgumentException e) { seenIae[i][0] = true; msgs[i] = e.getMessage(); }
        }
        for (int i = 0; i < feats.length; i++)
            check(seenIae[i][0], "with(" + feats[i] + ", '" + values[i] + "') -> IllegalArgumentException");
        check(msgs[0] != null && msgs[0].contains("bogus") && msgs[0].contains("PREFER")
              && msgs[0].contains("JAXP09020005"),
              "PREFER validation message carries JAXP09020005, value and feature name");
        // non-absolute FILES URI is rejected at with() time
        boolean iaeRel = false;
        String relMsg = null;
        try { javax.xml.catalog.CatalogFeatures.builder().with(javax.xml.catalog.CatalogFeatures.Feature.FILES, "relative.xml"); }
        catch (IllegalArgumentException e) { iaeRel = true; relMsg = e.getMessage(); }
        check(iaeRel, "with(FILES, relative URI) -> IllegalArgumentException");
        check(relMsg != null && relMsg.contains("not absolute"), "relative FILES message cites 'not absolute'");
        // builder() returns a fresh builder each call
        check(javax.xml.catalog.CatalogFeatures.builder() != javax.xml.catalog.CatalogFeatures.builder(),
              "builder() returns fresh instances");
    }
}
