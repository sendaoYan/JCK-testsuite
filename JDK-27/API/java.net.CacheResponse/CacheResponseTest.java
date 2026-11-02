/*
 * @test
 * @summary java.net.CacheResponse conformance: API type verification.
 * @run main CacheResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CacheResponseTest {
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
        Class<?> c = Class.forName("java.net.CacheResponse");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getHeaders"), "getHeaders present");
check(hasMethod(c, "getBody"), "getBody present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
