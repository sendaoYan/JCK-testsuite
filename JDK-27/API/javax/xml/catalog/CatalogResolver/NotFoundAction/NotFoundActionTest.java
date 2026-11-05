/*
 * @test
 * @summary javax.xml.catalog.CatalogResolver.NotFoundAction conformance: white-box structure + black-box behavior.
 * @run main NotFoundActionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NotFoundActionTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.CatalogResolver$NotFoundAction");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "superclass Enum");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "CONTINUE"), "field CONTINUE");
        check(hasField(c, "IGNORE"), "field IGNORE");
        check(hasField(c, "STRICT"), "field STRICT");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 3, "3 enum constants");
        check(hasField(c, "CONTINUE"), "enum constant CONTINUE");
        check(hasField(c, "IGNORE"), "enum constant IGNORE");
        check(hasField(c, "STRICT"), "enum constant STRICT");

        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("strict") == javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT, "getType");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.valueOf("CONTINUE") == javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE, "valueOf");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(javax.xml.catalog.CatalogResolver.NotFoundAction.valueOf("STRICT") == javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT, "valueOf STRICT");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.valueOf("CONTINUE") == javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE, "valueOf CONTINUE");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.valueOf("IGNORE") == javax.xml.catalog.CatalogResolver.NotFoundAction.IGNORE, "valueOf IGNORE");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("strict") == javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT, "getType strict");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.values().length == 3, "three constants");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (enum surface + getType matrix) ----
    static void testEnumSurface() {
        javax.xml.catalog.CatalogResolver.NotFoundAction[] vals =
            javax.xml.catalog.CatalogResolver.NotFoundAction.values();
        // canonical order verified against javap: CONTINUE, IGNORE, STRICT
        check(vals.length == 3, "exactly three actions");
        check(vals[0] == javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE
              && vals[1] == javax.xml.catalog.CatalogResolver.NotFoundAction.IGNORE
              && vals[2] == javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT,
              "declaration order CONTINUE, IGNORE, STRICT");
        // ordinals 0..2
        for (int i = 0; i < vals.length; i++)
            check(vals[i].ordinal() == i, vals[i].name() + " ordinal " + i);
        // toString is overridden to the lowercase literal (locked; differs from name())
        check("continue".equals(javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE.toString()),
              "CONTINUE toString == 'continue' (overridden)");
        check("ignore".equals(javax.xml.catalog.CatalogResolver.NotFoundAction.IGNORE.toString()),
              "IGNORE toString == 'ignore'");
        check("strict".equals(javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT.toString()),
              "STRICT toString == 'strict'");
        // toString value feeds getType (bidirectional mapping)
        for (javax.xml.catalog.CatalogResolver.NotFoundAction v : vals)
            check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType(v.toString()) == v,
                  "getType(toString(" + v.name() + ")) round-trip");
        // valueOf round-trips
        for (javax.xml.catalog.CatalogResolver.NotFoundAction v : vals)
            check(javax.xml.catalog.CatalogResolver.NotFoundAction.valueOf(v.name()) == v,
                  "valueOf('" + v.name() + "') round-trip");
        // getType: case-sensitive lowercase mapping
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("continue")
              == javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE, "getType('continue')");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("ignore")
              == javax.xml.catalog.CatalogResolver.NotFoundAction.IGNORE, "getType('ignore')");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("strict")
              == javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT, "getType('strict')");
        // getType rejections: uppercase, unknown, null
        String[] bad = {"CONTINUE", "Ignore", "bogus", ""};
        for (String s : bad) {
            boolean iae = false;
            String msg = null;
            try { javax.xml.catalog.CatalogResolver.NotFoundAction.getType(s); }
            catch (IllegalArgumentException e) { iae = true; msg = e.getMessage(); }
            check(iae, "getType('" + s + "') -> IllegalArgumentException");
            check(msg != null && msg.contains("JAXP09020005"),
                  "getType('" + s + "') message carries JAXP09020005");
        }
        boolean iaeNull = false;
        try { javax.xml.catalog.CatalogResolver.NotFoundAction.getType(null); }
        catch (IllegalArgumentException e) { iaeNull = true; }
        check(iaeNull, "getType(null) -> IllegalArgumentException");
        // valueOf unknown -> IAE (plain Enum behavior)
        boolean iaeValueOf = false;
        try { javax.xml.catalog.CatalogResolver.NotFoundAction.valueOf("bogus"); }
        catch (IllegalArgumentException e) { iaeValueOf = true; }
        check(iaeValueOf, "valueOf('bogus') -> IllegalArgumentException");
    }
}
