/*
 * @test
 * @summary com.sun.net.httpserver.HttpsConfigurator conformance: white-box structure + black-box behavior.
 * @run main HttpsConfiguratorTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;

public class HttpsConfiguratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.HttpsConfigurator");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getConstructor(SSLContext.class) != null, "(SSLContext) constructor");
        Set<String> m = methodNames(c);
        check(m.contains("getSSLContext"), "getSSLContext");
        check(m.contains("configure"), "configure");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // construct with default context and read it back
        SSLContext def = SSLContext.getDefault();
        com.sun.net.httpserver.HttpsConfigurator hc = new com.sun.net.httpserver.HttpsConfigurator(def);
        check(hc.getSSLContext() == def, "getSSLContext returns constructor arg");

        // construct with a specific TLS context
        SSLContext tls = SSLContext.getInstance("TLS");
        com.sun.net.httpserver.HttpsConfigurator hc2 = new com.sun.net.httpserver.HttpsConfigurator(tls);
        check(hc2.getSSLContext() == tls, "getSSLContext returns TLS context");

        // null constructor rejected
        boolean threw = false;
        try { new com.sun.net.httpserver.HttpsConfigurator(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null SSLContext throws NPE");

        // configure() invokes the overridable method with the given HttpsParameters;
        // default implementation calls setSSLParameters with the context's default params.
        final com.sun.net.httpserver.HttpsParameters[] seen = new com.sun.net.httpserver.HttpsParameters[1];
        com.sun.net.httpserver.HttpsParameters params = new com.sun.net.httpserver.HttpsParameters() {
            public com.sun.net.httpserver.HttpsConfigurator getHttpsConfigurator() { return hc; }
            public java.net.InetSocketAddress getClientAddress() {
                return new java.net.InetSocketAddress("127.0.0.1", 1234);
            }
            public void setSSLParameters(SSLParameters p) { }
        };
        com.sun.net.httpserver.HttpsConfigurator override = new com.sun.net.httpserver.HttpsConfigurator(def) {
            public void configure(com.sun.net.httpserver.HttpsParameters p) { seen[0] = p; }
        };
        override.configure(params);
        check(seen[0] == params, "configure dispatches to override with params");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
