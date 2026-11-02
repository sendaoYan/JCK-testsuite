/*
 * @test
 * @summary javax.naming.ldap.StartTlsResponse conformance: API type verification.
 * @run main StartTlsResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StartTlsResponseTest {
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
        Class<?> c = Class.forName("javax.naming.ldap.StartTlsResponse");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getID"), "getID present");
check(hasMethod(c, "getEncodedValue"), "getEncodedValue present");
check(hasMethod(c, "setEnabledCipherSuites"), "setEnabledCipherSuites present");
check(hasMethod(c, "setHostnameVerifier"), "setHostnameVerifier present");
check(hasMethod(c, "negotiate"), "negotiate present");
check(hasMethod(c, "close"), "close present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
