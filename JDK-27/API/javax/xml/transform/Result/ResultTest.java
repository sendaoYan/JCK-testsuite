/*
 * @test
 * @summary javax.xml.transform.Result conformance: white-box structure + black-box behavior.
 * @run main ResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ResultTest {
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

        Class<?> c = Class.forName("javax.xml.transform.Result");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "PI_DISABLE_OUTPUT_ESCAPING"), "field PI_DISABLE_OUTPUT_ESCAPING");
        check(hasField(c, "PI_ENABLE_OUTPUT_ESCAPING"), "field PI_ENABLE_OUTPUT_ESCAPING");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING.equals("javax.xml.transform.disable-output-escaping"), "PI_DISABLE_OUTPUT_ESCAPING");
        check(javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING.equals("javax.xml.transform.enable-output-escaping"), "PI_ENABLE_OUTPUT_ESCAPING");
        javax.xml.transform.Result a = new javax.xml.transform.Result(){
            public void setSystemId(java.lang.String p0) { }
            public java.lang.String getSystemId() { return "s"; }
        };
        check(a != null, "anon impl of Result");
        a.setSystemId("s");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
