/*
 * @test
 * @summary com.sun.net.httpserver.SimpleFileServer conformance: static factories.
 * @run main SimpleFileServerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SimpleFileServerTest {
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
        Class<?> c = load("com.sun.net.httpserver.SimpleFileServer");
        check(Modifier.isFinal(c.getModifiers()), "final");
        com.sun.net.httpserver.HttpHandler h = com.sun.net.httpserver.SimpleFileServer.createFileHandler(java.nio.file.Path.of("/tmp"));
        check(h != null, "createFileHandler non-null");
        com.sun.net.httpserver.Filter f = com.sun.net.httpserver.SimpleFileServer.createOutputFilter(System.out, com.sun.net.httpserver.SimpleFileServer.OutputLevel.INFO);
        check(f != null, "createOutputFilter non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
