/*
 * @test
 * @summary javax.xml.xpath.XPathEvaluationResult.XPathResultType conformance: API type verification.
 * @run main XPathResultTypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathResultTypeTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathEvaluationResult$XPathResultType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "superclass Enum");
        check(hasMethod(c, "getQNameType"), "method getQNameType");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "ANY"), "field ANY");
        check(hasField(c, "BOOLEAN"), "field BOOLEAN");
        check(hasField(c, "NODE"), "field NODE");
        check(hasField(c, "NODESET"), "field NODESET");
        check(hasField(c, "NUMBER"), "field NUMBER");
        check(hasField(c, "STRING"), "field STRING");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 6, "6 enum constants");
        check(hasField(c, "ANY"), "enum constant ANY");
        check(hasField(c, "BOOLEAN"), "enum constant BOOLEAN");
        check(hasField(c, "NODE"), "enum constant NODE");
        check(hasField(c, "NODESET"), "enum constant NODESET");
        check(hasField(c, "NUMBER"), "enum constant NUMBER");
        check(hasField(c, "STRING"), "enum constant STRING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
