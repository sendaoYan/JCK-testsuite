/*
 * @test
 * @summary javax.naming.NamingSecurityException conformance: API type verification.
 * @run main NamingSecurityExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NamingSecurityExceptionTest {
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
        Class<?> c = Class.forName("javax.naming.NamingSecurityException");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(c.getSuperclass().getName().equals("javax.naming.NamingException"), "extends javax.naming.NamingException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
