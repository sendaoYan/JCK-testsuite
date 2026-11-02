/*
 * @test
 * @summary java.net.SocketImpl conformance: API type verification.
 * @run main SocketImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketImplTest {
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
        Class<?> c = Class.forName("java.net.SocketImpl");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "toString"), "toString present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
