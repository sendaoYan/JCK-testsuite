/*
 * @test
 * @summary com.sun.security.auth.module.LdapLoginModule conformance: LoginModule lifecycle.
 * @run main LdapLoginModuleTest
 */

import java.lang.reflect.*;
import java.util.*;

public class LdapLoginModuleTest {
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
        Class<?> c = load("com.sun.security.auth.module.LdapLoginModule");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.security.auth.spi.LoginModule"), "LoginModule");
        Set<String> m = methodNames(c);
        check(m.contains("initialize"), "initialize");
        check(m.contains("login"), "login");
        check(m.contains("commit"), "commit");
        check(m.contains("abort"), "abort");
        check(m.contains("logout"), "logout");
        com.sun.security.auth.module.LdapLoginModule mod = new com.sun.security.auth.module.LdapLoginModule();
        java.util.Map<String,Object> opts = new java.util.HashMap<>();
        javax.security.auth.Subject subject = new javax.security.auth.Subject();
        mod.initialize(subject, null, opts, opts);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
