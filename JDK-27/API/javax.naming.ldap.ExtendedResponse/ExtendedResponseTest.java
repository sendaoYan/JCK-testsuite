/*
 * @test
 * @summary javax.naming.ldap.ExtendedResponse conformance: API type verification.
 * @run main ExtendedResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ExtendedResponseTest {
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
        Class<?> c = Class.forName("javax.naming.ldap.ExtendedResponse");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "getID"), "getID present");
check(hasMethod(c, "getEncodedValue"), "getEncodedValue present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
