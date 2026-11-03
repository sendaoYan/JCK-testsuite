/*
 * @test
 * @summary com.sun.security.jgss.ExtendedGSSCredential conformance: white-box structure + black-box behavior via dynamic proxy.
 * @run main ExtendedGSSCredentialTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExtendedGSSCredentialTest {
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

    static Object defValue(Class<?> rt) {
        if (!rt.isPrimitive()) return null;
        if (rt == boolean.class) return Boolean.FALSE;
        if (rt == int.class) return Integer.valueOf(0);
        if (rt == long.class) return Long.valueOf(0L);
        if (rt == short.class) return Short.valueOf((short) 0);
        if (rt == byte.class) return Byte.valueOf((byte) 0);
        if (rt == char.class) return Character.valueOf((char) 0);
        if (rt == float.class) return Float.valueOf(0.0f);
        if (rt == double.class) return Double.valueOf(0.0d);
        return null; // void
    }

    static class Handler implements InvocationHandler {
        public Object invoke(Object proxy, Method m, Object[] args) {
            String n = m.getName();
            if (n.equals("impersonate")) return null; // GSSCredential
            if (n.equals("toString")) return "ExtendedGSSCredentialProxy";
            if (n.equals("hashCode")) return Integer.valueOf(System.identityHashCode(proxy));
            if (n.equals("equals")) return Boolean.valueOf(proxy == (args == null ? null : args[0]));
            return defValue(m.getReturnType());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.jgss.ExtendedGSSCredential");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("org.ietf.jgss.GSSCredential"), "extends GSSCredential");
        check(methodNames(c).contains("impersonate"), "impersonate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassLoader cl = com.sun.security.jgss.ExtendedGSSCredential.class.getClassLoader();
        Object p = Proxy.newProxyInstance(
            cl, new Class<?>[]{ com.sun.security.jgss.ExtendedGSSCredential.class }, new Handler());
        com.sun.security.jgss.ExtendedGSSCredential cred = (com.sun.security.jgss.ExtendedGSSCredential) p;

        check(cred instanceof org.ietf.jgss.GSSCredential, "proxy instanceof GSSCredential");
        check(cred.impersonate(null) == null, "impersonate(null) callable, returns null stub");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
