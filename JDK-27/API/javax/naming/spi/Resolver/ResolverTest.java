/*
 * @test
 * @summary javax.naming.spi.Resolver conformance: white-box structure + black-box behavior.
 * @run main ResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ResolveResult;
import javax.naming.spi.Resolver;
import javax.naming.CompositeName;

public class ResolverTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.Resolver");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "resolveToClass", Name.class, Class.class),
                "resolveToClass(Name,Class)");
        check(hasMethod(c, "resolveToClass", String.class, Class.class),
                "resolveToClass(String,Class)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final ResolveResult result = new ResolveResult("resolved", "rest");
        final Object[] seen = new Object[2];
        Resolver resolver = new Resolver() {
            public ResolveResult resolveToClass(Name name,
                    Class<? extends Context> contextType) {
                seen[0] = name;
                return result;
            }
            public ResolveResult resolveToClass(String name,
                    Class<? extends Context> contextType) {
                seen[1] = name;
                return result;
            }
        };

        ResolveResult rName = resolver.resolveToClass(new javax.naming.CompositeName("a"), null);
        check(rName == result, "resolveToClass(Name,...) returns produced result");
        check(seen[0] instanceof Name, "Name overload invoked");

        ResolveResult rStr = resolver.resolveToClass("b", null);
        check(rStr == result, "resolveToClass(String,...) returns produced result");
        check("b".equals(seen[1]), "String overload receives the string");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dResBehavior();
        System.out.println("deep: Resolver contracts OK");
    }

    static class DeepResolver implements Resolver {
        static Name lastName;
        static String lastString;
        static Class<?> lastType;
        static int calls;
        public javax.naming.spi.ResolveResult resolveToClass(Name name, Class<? extends Context> ctxType) {
            lastName = name; lastType = ctxType; calls++;
            return new javax.naming.spi.ResolveResult("R(name," + ctxType.getSimpleName() + ")", "");
        }
        public javax.naming.spi.ResolveResult resolveToClass(String name, Class<? extends Context> ctxType) {
            lastString = name; lastType = ctxType; calls++;
            return new javax.naming.spi.ResolveResult("R(str," + ctxType.getSimpleName() + ")", "");
        }
    }

    static void dResBehavior() throws Exception {
        DeepResolver r = new DeepResolver();
        Name n = new CompositeName("a/b/c");
        check("R(name,Context)".equals(r.resolveToClass(n, Context.class).getResolvedObj()),
                "Name overload dispatches");
        check(DeepResolver.lastName.equals(n), "Name forwarded verbatim");
        check(DeepResolver.lastType == Context.class, "target type forwarded");
        check("R(str,Context)".equals(r.resolveToClass("x/y", Context.class).getResolvedObj()),
                "String overload dispatches");
        check("x/y".equals(DeepResolver.lastString), "String forwarded verbatim");
        check(DeepResolver.calls == 2, "both overloads callable");
        // null arguments are passed through (interface makes no null demands)
        check("R(str,Context)".equals(r.resolveToClass((String) null, Context.class).getResolvedObj()),
                "null String tolerated by contract of implementation");
    }

}
