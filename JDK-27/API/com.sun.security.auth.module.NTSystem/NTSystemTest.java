/*
 * @test
 * @summary com.sun.security.auth.module.NTSystem conformance: native accessor shape (reflection).
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
        Class<?> c = load("com.sun.security.auth.module.NTSystem");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        String[] names = {"getName","getDomain","getDomainSID","getUserSID","getPrimaryGroupID","getGroupIDs","getImpersonationToken"};
        for (String n : names) check(m.contains(n), n);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
