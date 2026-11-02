/*
 * @test
 * @summary javax.xml.crypto.dsig.TransformService conformance: API type verification.
 * @run main TransformServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformServiceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.TransformService");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.Transform"), "interface javax.xml.crypto.dsig.Transform");
        check(hasMethod(c, "getAlgorithm"), "method getAlgorithm");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getMechanismType"), "method getMechanismType");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "init"), "method init");
        check(hasMethod(c, "marshalParams"), "method marshalParams");

        javax.xml.crypto.dsig.TransformService ts = javax.xml.crypto.dsig.TransformService.getInstance(javax.xml.crypto.dsig.Transform.BASE64, "DOM");
        check("DOM".equals(ts.getMechanismType()), "mechanism");
        check(javax.xml.crypto.dsig.Transform.BASE64.equals(ts.getAlgorithm()), "algorithm");
        check(ts.getProvider() != null, "provider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
