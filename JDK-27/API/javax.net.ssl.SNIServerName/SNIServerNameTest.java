/*
 * @test
 * @summary javax.net.ssl.SNIServerName conformance: API type verification.
 * @run main SNIServerNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SNIServerNameTest {
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
        Class<?> c = Class.forName("javax.net.ssl.SNIServerName");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getType"), "getType present");
check(hasMethod(c, "getEncoded"), "getEncoded present");
check(hasMethod(c, "equals"), "equals present");
check(hasMethod(c, "hashCode"), "hashCode present");
check(hasMethod(c, "toString"), "toString present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
