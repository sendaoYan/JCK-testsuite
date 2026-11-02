/*
 * @test
 * @summary javax.net.ssl.SNIMatcher conformance: API type verification.
 * @run main SNIMatcherTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SNIMatcherTest {
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
        Class<?> c = Class.forName("javax.net.ssl.SNIMatcher");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getType"), "getType present");
check(hasMethod(c, "matches"), "matches present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
