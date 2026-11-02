/*
 * @test
 * @summary javax.net.ssl.KeyStoreBuilderParameters conformance: API type verification.
 * @run main KeyStoreBuilderParametersTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class KeyStoreBuilderParametersTest {
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
        Class<?> c = Class.forName("javax.net.ssl.KeyStoreBuilderParameters");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(hasMethod(c, "getParameters"), "getParameters present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
