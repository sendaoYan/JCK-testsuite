/*
 * @test
 * @summary com.sun.net.httpserver.spi.HttpServerProvider conformance: provider factory.
 * @run main HttpServerProviderTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpServerProviderTest {
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
        Class<?> c = load("com.sun.net.httpserver.spi.HttpServerProvider");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        com.sun.net.httpserver.spi.HttpServerProvider p = com.sun.net.httpserver.spi.HttpServerProvider.provider();
        check(p != null, "provider non-null");
        com.sun.net.httpserver.HttpServer s = p.createHttpServer(new java.net.InetSocketAddress(0), 0);
        check(s != null, "createHttpServer non-null");
        check(s.getAddress().getPort() > 0, "ephemeral port");
        s.stop(0);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
