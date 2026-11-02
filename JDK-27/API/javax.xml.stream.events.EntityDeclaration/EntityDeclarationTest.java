/*
 * @test
 * @summary javax.xml.stream.events.EntityDeclaration conformance: API type verification.
 * @run main EntityDeclarationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EntityDeclarationTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.EntityDeclaration");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getBaseURI"), "method getBaseURI");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getNotationName"), "method getNotationName");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getReplacementText"), "method getReplacementText");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
