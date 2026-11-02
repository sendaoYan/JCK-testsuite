/*
 * @test
 * @summary com.sun.net.httpserver.HttpsParameters conformance: abstract parameters.
 * @run main HttpsParametersTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpsParametersTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpsParameters");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"getHttpsConfigurator","getClientAddress","setSSLParameters","getCipherSuites","setCipherSuites","getProtocols","setProtocols","getWantClientAuth","setWantClientAuth","getNeedClientAuth","setNeedClientAuth"};
        for (String n : names) check(m.contains(n), n);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
