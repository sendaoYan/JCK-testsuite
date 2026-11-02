/*
 * @test
 * @summary java.net.CacheRequest conformance: API type verification.
 * @run main CacheRequestTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CacheRequestTest {
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
        Class<?> c = Class.forName("java.net.CacheRequest");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getBody"), "getBody present");
check(hasMethod(c, "abort"), "abort present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
