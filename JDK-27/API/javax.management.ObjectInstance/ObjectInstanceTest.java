/*
 * @test
 * @summary javax.management.ObjectInstance conformance: white-box structure + black-box behavior.
 * @run main ObjectInstanceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class ObjectInstanceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ObjectInstance");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "getObjectName"), "has getObjectName");
        check(hasMethod(c, "getClassName"), "has getClassName");
        check(hasMethod(c, "equals"), "has equals");
        check(hasMethod(c, "hashCode"), "has hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectInstance oi = new ObjectInstance("domain:key=value", "java.lang.String");
        check(oi.getObjectName().equals(new ObjectName("domain:key=value")), "getObjectName");
        check("java.lang.String".equals(oi.getClassName()), "getClassName");

        // ObjectName-based constructor
        ObjectName on = new ObjectName("d:k=v");
        ObjectInstance oi2 = new ObjectInstance(on, "java.lang.Integer");
        check(oi2.getObjectName().equals(on), "ObjectName ctor object name");
        check("java.lang.Integer".equals(oi2.getClassName()), "ObjectName ctor class name");

        // equals compares object name and class name
        check(oi.equals(new ObjectInstance("domain:key=value", "java.lang.String")), "equals identical");
        check(!oi.equals(new ObjectInstance("domain:key=value", "java.lang.Integer")), "different class name not equal");
        check(!oi.equals(new ObjectInstance("domain:key=other", "java.lang.String")), "different object name not equal");
        check(!oi.equals(null), "not equal null");
        check(oi.hashCode() == new ObjectInstance("domain:key=value", "java.lang.String").hashCode(), "equal hashCode");

        // toString contains both components
        String s = oi.toString();
        check(s != null && s.contains("domain") && s.contains("java.lang.String"), "toString contains parts");

        // malformed object name in String constructor throws MalformedObjectNameException
        boolean threw = false;
        try { new ObjectInstance(":", "java.lang.String"); } catch (MalformedObjectNameException ex) { threw = true; }
        check(threw, "malformed name throws MalformedObjectNameException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
