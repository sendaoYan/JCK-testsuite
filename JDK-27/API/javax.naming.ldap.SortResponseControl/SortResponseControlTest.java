/*
 * @test
 * @summary javax.naming.ldap.SortResponseControl conformance: API type verification.
 * @run main SortResponseControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SortResponseControlTest {
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
        Class<?> c = Class.forName("javax.naming.ldap.SortResponseControl");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.getSuperclass().getName().equals("javax.naming.ldap.BasicControl"), "extends javax.naming.ldap.BasicControl");
check(hasMethod(c, "isSorted"), "isSorted present");
check(hasMethod(c, "getResultCode"), "getResultCode present");
check(hasMethod(c, "getAttributeID"), "getAttributeID present");
check(hasMethod(c, "getException"), "getException present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
