/*
 * @test
 * @summary javax.net.ssl.HttpsURLConnection conformance: API type verification.
 * @run main HttpsURLConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class HttpsURLConnectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory() != null, "default SSL socket factory");
check(javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier() != null, "default hostname verifier");
check(java.lang.reflect.Modifier.isAbstract(javax.net.ssl.HttpsURLConnection.class.getModifiers()), "abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
