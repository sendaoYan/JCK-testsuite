/*
 * @test
 * @summary java.net.URLStreamHandler conformance: API type verification.
 * @run main URLStreamHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLStreamHandlerTest {
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
        Class<?> c = Class.forName("java.net.URLStreamHandler");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
