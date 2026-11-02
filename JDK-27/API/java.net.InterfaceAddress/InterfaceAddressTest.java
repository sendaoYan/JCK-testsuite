/*
 * @test
 * @summary java.net.InterfaceAddress conformance: API type verification.
 * @run main InterfaceAddressTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InterfaceAddressTest {
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
        Class<?> c = Class.forName("java.net.InterfaceAddress");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(hasMethod(c, "getAddress"), "getAddress present");
check(hasMethod(c, "getBroadcast"), "getBroadcast present");
check(hasMethod(c, "getNetworkPrefixLength"), "getNetworkPrefixLength present");
check(hasMethod(c, "equals"), "equals present");
check(hasMethod(c, "hashCode"), "hashCode present");
check(hasMethod(c, "toString"), "toString present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
