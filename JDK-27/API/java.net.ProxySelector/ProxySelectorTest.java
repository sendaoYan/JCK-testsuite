/*
 * @test
 * @summary java.net.ProxySelector conformance: API type verification.
 * @run main ProxySelectorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ProxySelectorTest {
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
        Class<?> c = Class.forName("java.net.ProxySelector");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getDefault"), "getDefault present");
check(hasMethod(c, "setDefault"), "setDefault present");
check(hasMethod(c, "select"), "select present");
check(hasMethod(c, "connectFailed"), "connectFailed present");
check(hasMethod(c, "of"), "of present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
