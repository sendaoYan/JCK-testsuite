/*
 * @test
 * @summary com.sun.net.httpserver.HttpsConfigurator conformance: SSL context.
 * @run main HttpsConfiguratorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpsConfiguratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getDefault();
        com.sun.net.httpserver.HttpsConfigurator hc = new com.sun.net.httpserver.HttpsConfigurator(ctx);
        check(hc.getSSLContext() == ctx, "getSSLContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
