/*
 * @test
 * @summary javax.xml.transform.dom.DOMResult conformance: API type verification.
 * @run main DOMResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMResultTest {
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
        Class<?> c = Class.forName("javax.xml.transform.dom.DOMResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getNextSibling"), "method getNextSibling");
        check(hasMethod(c, "getNode"), "method getNode");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "setNextSibling"), "method setNextSibling");
        check(hasMethod(c, "setNode"), "method setNode");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.dom.DOMResult r = new javax.xml.transform.dom.DOMResult();
        check(r.getNode() == null, "node null");
        r.setSystemId("sys");
        check("sys".equals(r.getSystemId()), "systemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
