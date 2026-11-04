/*
 * @test
 * @summary com.sun.security.auth.module.NTSystem conformance: white-box structure + black-box behavior.
 * @run main NTSystemTest
 */

import java.lang.reflect.*;
import java.util.*;

public class NTSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.module.NTSystem");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        String[] names = {"getName","getDomain","getDomainSID","getUserSID",
                          "getPrimaryGroupID","getGroupIDs","getImpersonationToken"};
        for (String n : names) check(m.contains(n), n);
    }

    static void testBlackBox() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // Windows: the native implementation is available.
            com.sun.security.auth.module.NTSystem n = new com.sun.security.auth.module.NTSystem();
            check(n != null, "constructed on Windows");
            // Accessors are callable; values may be null when not domain-joined.
            n.getName();
            n.getDomain();
            n.getDomainSID();
            n.getUserSID();
            n.getPrimaryGroupID();
            n.getGroupIDs();
            n.getImpersonationToken();
            check(true, "Windows accessors callable without exception");
        } else {
            // Non-Windows: JDK-27 ships the native "jaas" library only for
            // Windows (nt.c), so constructing NTSystem throws UnsatisfiedLinkError.
            boolean threw = false;
            try {
                new com.sun.security.auth.module.NTSystem();
            } catch (UnsatisfiedLinkError e) {
                threw = true;
            }
            check(threw, "non-Windows NTSystem construction throws UnsatisfiedLinkError");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
