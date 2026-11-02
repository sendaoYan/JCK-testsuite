/*
 * @test
 * @summary javax.xml.catalog.CatalogResolver.NotFoundAction conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
