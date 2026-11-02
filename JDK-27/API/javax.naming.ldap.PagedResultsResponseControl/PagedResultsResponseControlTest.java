/*
 * @test
 * @summary javax.naming.ldap.PagedResultsResponseControl conformance: API type verification.
 * @run main PagedResultsResponseControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class PagedResultsResponseControlTest {
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
        Class<?> c = Class.forName("javax.naming.ldap.PagedResultsResponseControl");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.getSuperclass().getName().equals("javax.naming.ldap.BasicControl"), "extends javax.naming.ldap.BasicControl");
check(hasMethod(c, "getResultSize"), "getResultSize present");
check(hasMethod(c, "getCookie"), "getCookie present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
