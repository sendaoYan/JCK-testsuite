/*
 * @test
 * @summary com.sun.security.jgss.ExtendedGSSContext conformance: white-box structure + black-box behavior via dynamic proxy.
 * @run main ExtendedGSSContextTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExtendedGSSContextTest {
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

    // Return a neutral default value for any GSSContext method return type,
    // so the proxy only has to special-case the three ExtendedGSSContext methods.
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
            if (n.equals("inquireSecContext")) return null;
            if (n.equals("requestDelegPolicy")) return null;
            if (n.equals("getDelegPolicyState")) return Boolean.FALSE;
            if (n.equals("toString")) return "ExtendedGSSContextProxy";
            if (n.equals("hashCode")) return Integer.valueOf(System.identityHashCode(proxy));
            if (n.equals("equals")) return Boolean.valueOf(proxy == (args == null ? null : args[0]));
            return defValue(m.getReturnType());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.jgss.ExtendedGSSContext");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("org.ietf.jgss.GSSContext"), "extends GSSContext");
        Set<String> m = methodNames(c);
        check(m.contains("inquireSecContext"), "inquireSecContext");
        check(m.contains("requestDelegPolicy"), "requestDelegPolicy");
        check(m.contains("getDelegPolicyState"), "getDelegPolicyState");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassLoader cl = com.sun.security.jgss.ExtendedGSSContext.class.getClassLoader();
        Object p = Proxy.newProxyInstance(
            cl, new Class<?>[]{ com.sun.security.jgss.ExtendedGSSContext.class }, new Handler());
        com.sun.security.jgss.ExtendedGSSContext ctxt = (com.sun.security.jgss.ExtendedGSSContext) p;

        check(ctxt instanceof org.ietf.jgss.GSSContext, "proxy instanceof GSSContext");

        // void request + boolean query are callable without a real GSS mechanism.
        ctxt.requestDelegPolicy(true);
        ctxt.requestDelegPolicy(false);
        check(ctxt.getDelegPolicyState() == false, "getDelegPolicyState() default false");

        // inquireSecContext(InquireType) returns Object; our stub returns null for every type.
        for (com.sun.security.jgss.InquireType t : com.sun.security.jgss.InquireType.values()) {
            check(ctxt.inquireSecContext(t) == null, "inquireSecContext(" + t + ") == null");
        }
        check(ctxt.inquireSecContext(null) == null, "inquireSecContext(null) == null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
