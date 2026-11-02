/*
 * @test
 * @summary javax.net.ssl.SSLSessionContext conformance: API type verification.
 * @run main SSLSessionContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLSessionContextTest {
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
        Class<?> c = Class.forName("javax.net.ssl.SSLSessionContext");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "getSession"), "getSession present");
check(hasMethod(c, "getIds"), "getIds present");
check(hasMethod(c, "setSessionTimeout"), "setSessionTimeout present");
check(hasMethod(c, "getSessionTimeout"), "getSessionTimeout present");
check(hasMethod(c, "setSessionCacheSize"), "setSessionCacheSize present");
check(hasMethod(c, "getSessionCacheSize"), "getSessionCacheSize present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
