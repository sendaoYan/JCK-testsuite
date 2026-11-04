/*
 * @test
 * @summary javax.management.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.Attribute");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getName"), "has getName");
        check(hasMethod(c, "getValue"), "has getValue");
        check(hasMethod(c, "equals"), "has equals");
        check(hasMethod(c, "hashCode"), "has hashCode");
        check(hasMethod(c, "toString"), "has toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Attribute a = new Attribute("name", "value");
        check("name".equals(a.getName()), "getName");
        check("value".equals(a.getValue()), "getValue");

        Attribute same = new Attribute("name", "value");
        check(a.equals(same), "equals symmetric");
        check(same.equals(a), "equals symmetric reverse");
        check(a.hashCode() == same.hashCode(), "equal hashCode");

        Attribute diffName = new Attribute("other", "value");
        Attribute diffValue = new Attribute("name", "other");
        check(!a.equals(diffName), "different name not equal");
        check(!a.equals(diffValue), "different value not equal");
        check(!a.equals(null), "not equal null");
        check(!a.equals("name"), "not equal non-Attribute");

        Attribute nullValue = new Attribute("name", null);
        check(nullValue.getValue() == null, "null value stored");
        check(nullValue.equals(new Attribute("name", null)), "null value equals");
        check(!nullValue.equals(a), "null vs non-null value not equal");

        Attribute nullName = null;
        boolean threwNull = false;
        try { nullName = new Attribute(null, "v"); } catch (RuntimeOperationsException ex) { threwNull = true; }
        check(threwNull, "null name throws RuntimeOperationsException");

        String s = a.toString();
        check(s.contains("name") && s.contains("value"), "toString contains name and value");

        // equals contract: reflexive and transitive
        Attribute a2 = new Attribute("name", "value");
        check(a.equals(a) && a.equals(a2) && a2.equals(a), "equals reflexive/transitive");
        check(a.equals(new Attribute("name", "value")), "new equal instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
