/*
 * @test
 * @requires os.family == "linux"
 * @summary com.sun.security.auth.module.UnixSystem conformance: white-box structure + black-box behavior.
 * @run main UnixSystemTest
 */

import java.lang.reflect.*;
import java.util.*;

public class UnixSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.module.UnixSystem");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        String[] names = {"getUsername","getUid","getGid","getGroups"};
        for (String n : names) check(m.contains(n), n);
    }

    static void testBlackBox() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // UnixSystem is Unix-only; on Windows it is expected to fail to load
            // the underlying native symbols (no getuid/getgroups).
            boolean threw = false;
            try {
                new com.sun.security.auth.module.UnixSystem();
            } catch (Throwable t) {
                threw = true;
            }
            check(threw, "Windows degraded: UnixSystem construction fails");
            return;
        }

        com.sun.security.auth.module.UnixSystem u = new com.sun.security.auth.module.UnixSystem();
        check(u.getUsername() != null, "username non-null");
        check(u.getUsername().length() > 0, "username non-empty");
        check(u.getUid() >= 0, "uid >= 0");
        check(u.getGid() >= 0, "gid >= 0");

        long[] groups = u.getGroups();
        check(groups != null, "groups non-null");
        for (long g : groups) check(g >= 0, "each group id >= 0");

        // getGroups() returns a defensive copy: a second call is a fresh array.
        long[] again = u.getGroups();
        check(again != null, "groups clone non-null");
        check(again.length == groups.length, "groups clone same length");
        check(again != groups, "groups returns a fresh array");

        check(u.getUid() == u.getUid(), "uid accessor stable");
        check(u.getGid() == u.getGid(), "gid accessor stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
