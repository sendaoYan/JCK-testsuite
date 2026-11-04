/*
 * @test
 * @summary com.sun.net.httpserver.Authenticator.Retry conformance: response code.
 * @run main RetryTest
 */

import java.lang.reflect.*;
import java.util.*;

public class RetryTest {
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
        com.sun.net.httpserver.Authenticator.Retry r = new com.sun.net.httpserver.Authenticator.Retry(407);
        check(r.getResponseCode() == 407, "responseCode 407");
        check(r instanceof com.sun.net.httpserver.Authenticator.Result, "extends Result");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
