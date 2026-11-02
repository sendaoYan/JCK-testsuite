/*
 * @test
 * @summary javax.net.ssl.ExtendedSSLSession conformance: API type verification.
 * @run main ExtendedSSLSessionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ExtendedSSLSessionTest {
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
        Class<?> c = Class.forName("javax.net.ssl.ExtendedSSLSession");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getLocalSupportedSignatureAlgorithms"), "getLocalSupportedSignatureAlgorithms present");
check(hasMethod(c, "getPeerSupportedSignatureAlgorithms"), "getPeerSupportedSignatureAlgorithms present");
check(hasMethod(c, "getRequestedServerNames"), "getRequestedServerNames present");
check(hasMethod(c, "getStatusResponses"), "getStatusResponses present");
check(hasMethod(c, "exportKeyingMaterialKey"), "exportKeyingMaterialKey present");
check(hasMethod(c, "exportKeyingMaterialData"), "exportKeyingMaterialData present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
