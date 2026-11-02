/*
 * @test
 * @summary java.net.ProtocolFamily conformance: API type verification.
 * @run main ProtocolFamilyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ProtocolFamilyTest {
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
        Class<?> c = Class.forName("java.net.ProtocolFamily");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "name"), "name present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
