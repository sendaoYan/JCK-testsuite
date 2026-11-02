/*
 * @test
 * @summary javax.naming.ldap.LdapContext conformance: API type verification.
 * @run main LdapContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LdapContextTest {
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
        Class<?> c = Class.forName("javax.naming.ldap.LdapContext");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "extendedOperation"), "extendedOperation present");
check(hasMethod(c, "newInstance"), "newInstance present");
check(hasMethod(c, "reconnect"), "reconnect present");
check(hasMethod(c, "getConnectControls"), "getConnectControls present");
check(hasMethod(c, "setRequestControls"), "setRequestControls present");
check(hasMethod(c, "getRequestControls"), "getRequestControls present");
check(hasMethod(c, "getResponseControls"), "getResponseControls present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
