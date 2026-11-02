/*
 * @test
 * @summary javax.net.ssl.X509TrustManager conformance: API type verification.
 * @run main X509TrustManagerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class X509TrustManagerTest {
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
        Class<?> c = Class.forName("javax.net.ssl.X509TrustManager");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "checkClientTrusted"), "checkClientTrusted present");
check(hasMethod(c, "checkServerTrusted"), "checkServerTrusted present");
check(hasMethod(c, "getAcceptedIssuers"), "getAcceptedIssuers present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
