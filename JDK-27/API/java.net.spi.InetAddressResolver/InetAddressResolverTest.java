/*
 * @test
 * @summary java.net.spi.InetAddressResolver conformance: API type verification.
 * @run main InetAddressResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InetAddressResolverTest {
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
        Class<?> c = Class.forName("java.net.spi.InetAddressResolver");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "lookupByName"), "lookupByName present");
check(hasMethod(c, "lookupByAddress"), "lookupByAddress present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
