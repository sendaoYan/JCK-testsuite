/*
 * @test
 * @summary java.net.CookieHandler conformance: API type verification.
 * @run main CookieHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CookieHandlerTest {
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
        Class<?> c = Class.forName("java.net.CookieHandler");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getDefault"), "getDefault present");
check(hasMethod(c, "setDefault"), "setDefault present");
check(hasMethod(c, "get"), "get present");
check(hasMethod(c, "put"), "put present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
