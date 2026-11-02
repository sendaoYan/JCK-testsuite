/*
 * @test
 * @summary javax.xml.catalog.GroupEntry.PreferType conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
