/*
 * @test
 * @summary javax.xml.catalog.GroupEntry.PreferType conformance: white-box structure + black-box behavior.
 * @run main PreferTypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class PreferTypeTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.GroupEntry$PreferType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "superclass Enum");
        check(hasMethod(c, "prefer"), "method prefer");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "PUBLIC"), "field PUBLIC");
        check(hasField(c, "SYSTEM"), "field SYSTEM");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 2, "2 enum constants");
        check(hasField(c, "PUBLIC"), "enum constant PUBLIC");
        check(hasField(c, "SYSTEM"), "enum constant SYSTEM");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // GroupEntry is package-private; PreferType is a public nested enum reachable by reflection
        Class<?> c = Class.forName("javax.xml.catalog.GroupEntry$PreferType");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 2, "two enum constants");
        check("PUBLIC".equals(((Enum<?>) ec[0]).name()) || "SYSTEM".equals(((Enum<?>) ec[0]).name()), "constant names");
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        check(((Enum<?>) valueOf.invoke(null, "PUBLIC")).name().equals("PUBLIC"), "valueOf PUBLIC");
        java.lang.reflect.Method prefer = c.getMethod("prefer", String.class);
        check(((Boolean) prefer.invoke(ec[0], "public")).booleanValue() == "PUBLIC".equals(((Enum<?>) ec[0]).name()), "prefer matches own name");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (enum surface + prefer() matrix) ----
    static void testEnumSurface() throws Exception {
        Class<?> c = Class.forName("javax.xml.catalog.GroupEntry$PreferType");
        Object[] vals = c.getEnumConstants();
        // canonical order verified against javap: PUBLIC, SYSTEM
        check(vals.length == 2, "exactly two PreferType constants");
        check(((Enum<?>) vals[0]).name().equals("PUBLIC") && ((Enum<?>) vals[1]).name().equals("SYSTEM"),
              "declaration order PUBLIC, SYSTEM");
        check(((Enum<?>) vals[0]).ordinal() == 0 && ((Enum<?>) vals[1]).ordinal() == 1,
              "ordinals 0 and 1");
        // toString is the constant name
        for (Object v : vals) check(v.toString().equals(((Enum<?>) v).name()), v + " toString == name");
        // valueOf round-trip
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        for (Object v : vals)
            check(((Enum<?>) valueOf.invoke(null, ((Enum<?>) v).name())).name().equals(((Enum<?>) v).name()),
                  "valueOf('" + ((Enum<?>) v).name() + "') round-trip");
        boolean iae = false;
        try { valueOf.invoke(null, "bogus"); } catch (java.lang.reflect.InvocationTargetException e) {
            iae = e.getCause() instanceof IllegalArgumentException;
        }
        check(iae, "valueOf('bogus') -> IllegalArgumentException");

        // prefer(String) answers membership: own literal true, other literal false, unknown false
        java.lang.reflect.Method prefer = c.getMethod("prefer", String.class);
        Object pub = vals[0], sys = vals[1];
        check((Boolean) prefer.invoke(pub, "public"), "PUBLIC.prefer('public') true");
        check(!(Boolean) prefer.invoke(pub, "system"), "PUBLIC.prefer('system') false");
        check((Boolean) prefer.invoke(sys, "system"), "SYSTEM.prefer('system') true");
        check(!(Boolean) prefer.invoke(sys, "public"), "SYSTEM.prefer('public') false");
        check(!(Boolean) prefer.invoke(pub, "bogus"), "PUBLIC.prefer('bogus') false (no exception)");
        check(!(Boolean) prefer.invoke(pub, "PUBLIC"), "prefer() is case sensitive");
        // GroupEntry itself is package-private
        Class<?> group = Class.forName("javax.xml.catalog.GroupEntry");
        check(!java.lang.reflect.Modifier.isPublic(group.getModifiers()), "GroupEntry is package-private");
    }
}
