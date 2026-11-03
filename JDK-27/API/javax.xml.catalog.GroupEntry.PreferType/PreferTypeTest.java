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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
