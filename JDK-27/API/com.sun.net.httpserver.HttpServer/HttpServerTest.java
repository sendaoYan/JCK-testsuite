/*
 * @test
 * @summary com.sun.net.httpserver.HttpServer conformance: create/bind/stop.
 * @run main HttpServerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpServerTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpServer");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"create","bind","start","stop","createContext","removeContext","getAddress"};
        for (String n : names) check(m.contains(n), n);
        com.sun.net.httpserver.HttpServer s = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(0), 0);
        check(s != null, "create non-null");
        check(s.getAddress().getPort() > 0, "ephemeral port > 0");
        s.stop(0);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
