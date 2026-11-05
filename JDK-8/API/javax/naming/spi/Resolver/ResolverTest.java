/*
 * @test
 * @summary javax.naming.spi.Resolver conformance: interface hierarchy and members.
 * @run main ResolverTest
 */



import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.Resolver;
public class ResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.Resolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolveToClass"), "has method resolveToClass");        testDeepContracts();
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
