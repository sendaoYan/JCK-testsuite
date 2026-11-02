/*
 * @test
 * @summary com.sun.security.auth.login.ConfigFile conformance: configuration.
 * @run main ConfigFileTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ConfigFileTest {
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
        Class<?> c = load("com.sun.security.auth.login.ConfigFile");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass().getName().equals("javax.security.auth.login.Configuration"), "extends Configuration");
        com.sun.security.auth.login.ConfigFile cf = new com.sun.security.auth.login.ConfigFile();
        check(cf.getAppConfigurationEntry("nonexistent") == null, "null entries");
        cf.refresh();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
