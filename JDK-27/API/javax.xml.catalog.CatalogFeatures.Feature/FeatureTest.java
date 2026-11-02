/*
 * @test
 * @summary javax.xml.catalog.CatalogFeatures.Feature conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
