/*
 * @test
 * @summary com.sun.net.httpserver.BasicAuthenticator conformance: realm and credentials.
 * @run main BasicAuthenticatorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class BasicAuthenticatorTest {
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
        Class<?> c = load("com.sun.net.httpserver.BasicAuthenticator");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.net.httpserver.Authenticator"), "extends Authenticator");
        com.sun.net.httpserver.BasicAuthenticator b = new com.sun.net.httpserver.BasicAuthenticator("realm") {
            public boolean checkCredentials(String u, String p) { return "user".equals(u) && "pass".equals(p); }
        };
        check(b.getRealm().equals("realm"), "realm");
        check(b.checkCredentials("user", "pass"), "checkCredentials true");
        check(!b.checkCredentials("user", "wrong"), "checkCredentials false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
