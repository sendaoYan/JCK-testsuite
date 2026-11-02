/*
 * @test
 * @summary javax.net.ssl.SSLSession conformance: API type verification.
 * @run main SSLSessionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLSessionTest {
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
        Class<?> c = Class.forName("javax.net.ssl.SSLSession");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "getId"), "getId present");
check(hasMethod(c, "getSessionContext"), "getSessionContext present");
check(hasMethod(c, "getCreationTime"), "getCreationTime present");
check(hasMethod(c, "getLastAccessedTime"), "getLastAccessedTime present");
check(hasMethod(c, "invalidate"), "invalidate present");
check(hasMethod(c, "isValid"), "isValid present");
check(hasMethod(c, "putValue"), "putValue present");
check(hasMethod(c, "getValue"), "getValue present");
check(hasMethod(c, "removeValue"), "removeValue present");
check(hasMethod(c, "getValueNames"), "getValueNames present");
check(hasMethod(c, "getPeerCertificates"), "getPeerCertificates present");
check(hasMethod(c, "getLocalCertificates"), "getLocalCertificates present");
check(hasMethod(c, "getPeerCertificateChain"), "getPeerCertificateChain present");
check(hasMethod(c, "getPeerPrincipal"), "getPeerPrincipal present");
check(hasMethod(c, "getLocalPrincipal"), "getLocalPrincipal present");
check(hasMethod(c, "getCipherSuite"), "getCipherSuite present");
check(hasMethod(c, "getProtocol"), "getProtocol present");
check(hasMethod(c, "getPeerHost"), "getPeerHost present");
check(hasMethod(c, "getPeerPort"), "getPeerPort present");
check(hasMethod(c, "getPacketBufferSize"), "getPacketBufferSize present");
check(hasMethod(c, "getApplicationBufferSize"), "getApplicationBufferSize present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
