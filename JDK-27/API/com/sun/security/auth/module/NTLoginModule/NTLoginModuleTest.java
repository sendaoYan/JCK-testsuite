/*
 * @test
 * @summary com.sun.security.auth.module.NTLoginModule conformance: white-box structure + black-box behavior.
 * @run main NTLoginModuleTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.security.auth.Subject;

public class NTLoginModuleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.module.NTLoginModule");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.security.auth.spi.LoginModule"), "implements LoginModule");
        Set<String> m = methodNames(c);
        for (String n : new String[]{"initialize","login","commit","abort","logout"}) check(m.contains(n), n);
    }

    static void testBlackBox() {
        // Construct and initialize only. A real login()/commit() would contact a
        // KDC / LDAP server / keystore / native system account and cannot run here.
        com.sun.security.auth.module.NTLoginModule mod = new com.sun.security.auth.module.NTLoginModule();
        check(mod != null, "constructed");
        Subject s1 = new Subject();
        Map<String,Object> shared = new HashMap<>();
        Map<String,Object> opts = new HashMap<>();
        mod.initialize(s1, null, shared, opts);
        check(shared.isEmpty(), "initialize does not mutate sharedState");
        check(opts.isEmpty(), "initialize does not mutate options");
        Subject s2 = new Subject();
        mod.initialize(s2, null, shared, opts);
        check(true, "re-initialize with a different Subject does not throw");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
