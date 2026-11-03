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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
