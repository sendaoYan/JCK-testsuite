/*
 * @test
 * @summary java.net.JarURLConnection conformance: API type verification.
 * @run main JarURLConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class JarURLConnectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
static boolean hasMethod(Class<?> c, String n) {
    for (java.lang.reflect.Method m : c.getMethods()) {
        if (m.getName().equals(n)) return true;
    }
    return false;
}

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.JarURLConnection");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("java.net.URLConnection"), "extends java.net.URLConnection");
check(hasMethod(c, "getJarFileURL"), "getJarFileURL present");
check(hasMethod(c, "getEntryName"), "getEntryName present");
check(hasMethod(c, "getJarFile"), "getJarFile present");
check(hasMethod(c, "getManifest"), "getManifest present");
check(hasMethod(c, "getJarEntry"), "getJarEntry present");
check(hasMethod(c, "getAttributes"), "getAttributes present");
check(hasMethod(c, "getMainAttributes"), "getMainAttributes present");
check(hasMethod(c, "getCertificates"), "getCertificates present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
