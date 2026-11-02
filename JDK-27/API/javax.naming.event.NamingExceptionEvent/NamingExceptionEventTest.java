/*
 * @test
 * @summary javax.naming.event.NamingExceptionEvent conformance: API type verification.
 * @run main NamingExceptionEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NamingExceptionEventTest {
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
        Class<?> c = Class.forName("javax.naming.event.NamingExceptionEvent");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.getSuperclass().getName().equals("java.util.EventObject"), "extends java.util.EventObject");
check(hasMethod(c, "getException"), "getException present");
check(hasMethod(c, "getEventContext"), "getEventContext present");
check(hasMethod(c, "dispatch"), "dispatch present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
