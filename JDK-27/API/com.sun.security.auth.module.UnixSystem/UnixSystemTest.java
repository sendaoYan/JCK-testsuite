/*
 * @test
 * @summary com.sun.security.auth.module.UnixSystem conformance: accessors.
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
        Class<?> c = load("com.sun.security.auth.module.UnixSystem");
        check(!c.isInterface(), "not an interface");
        com.sun.security.auth.module.UnixSystem u = new com.sun.security.auth.module.UnixSystem();
        check(u.getUsername() != null, "username non-null");
        check(u.getUid() >= 0, "uid >= 0");
        check(u.getGid() >= 0, "gid >= 0");
        check(u.getGroups() != null, "groups non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
