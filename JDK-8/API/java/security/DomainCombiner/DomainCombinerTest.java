/*
 * @test
 * @summary java.security.DomainCombiner conformance: interface hierarchy and key methods.
 *          Appended: exact combine(ProtectionDomain[],ProtectionDomain[]) signature returning
 *          ProtectionDomain[], functional-interface shape, identity/append/drop/null-tolerant
 *          combiners with verbatim array-content delivery, attachment to AccessControlContext
 *          and retrieval via getDomainCombiner.
 * @run main DomainCombinerTest
 */

public class DomainCombinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.DomainCombiner");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("combine"), "declares combine");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        // exact signature: single abstract combine(ProtectionDomain[], ProtectionDomain[]) -> ProtectionDomain[]
        java.lang.reflect.Method[] ms = java.security.DomainCombiner.class.getDeclaredMethods();
        check(ms.length == 1, "single method interface (functional)");
        java.lang.reflect.Method comb = ms[0];
        check(comb.getName().equals("combine"), "method name combine");
        Class<?>[] p = comb.getParameterTypes();
        check(p.length == 2 && p[0] == java.security.ProtectionDomain[].class
                && p[1] == java.security.ProtectionDomain[].class,
                "combine(ProtectionDomain[],ProtectionDomain[]) signature");
        check(comb.getReturnType() == java.security.ProtectionDomain[].class, "returns ProtectionDomain[]");
        check(!comb.isDefault(), "not a default method");

        java.security.ProtectionDomain pd =
                new java.security.ProtectionDomain(null, null);

        // identity combiner: returns first argument unchanged
        java.security.DomainCombiner identity = new java.security.DomainCombiner() {
            public java.security.ProtectionDomain[] combine(java.security.ProtectionDomain[] current,
                    java.security.ProtectionDomain[] assigned) { return current; }
        };
        java.security.ProtectionDomain[] one = {pd};
        check(identity.combine(one, null) == one, "identity returns current array identity");
        check(identity.combine(null, null) == null, "identity combine(null,null) tolerated");

        // drop combiner returns an empty result regardless of inputs
        java.security.DomainCombiner dropper = new java.security.DomainCombiner() {
            public java.security.ProtectionDomain[] combine(java.security.ProtectionDomain[] current,
                    java.security.ProtectionDomain[] assigned) {
                return new java.security.ProtectionDomain[0];
            }
        };
        check(dropper.combine(new java.security.ProtectionDomain[]{pd, pd},
                new java.security.ProtectionDomain[]{pd}).length == 0, "dropper returns empty");

        // append combiner: concatenates, preserving element identity and order
        java.security.DomainCombiner appender = new java.security.DomainCombiner() {
            public java.security.ProtectionDomain[] combine(java.security.ProtectionDomain[] current,
                    java.security.ProtectionDomain[] assigned) {
                java.security.ProtectionDomain[] out =
                        new java.security.ProtectionDomain[(current == null ? 0 : current.length)
                                + (assigned == null ? 0 : assigned.length)];
                int i = 0;
                if (current != null) for (java.security.ProtectionDomain d : current) out[i++] = d;
                if (assigned != null) for (java.security.ProtectionDomain d : assigned) out[i++] = d;
                return out;
            }
        };
        java.security.ProtectionDomain pd2 = new java.security.ProtectionDomain(null, null);
        java.security.ProtectionDomain[] merged = appender.combine(
                new java.security.ProtectionDomain[]{pd}, new java.security.ProtectionDomain[]{pd2});
        check(merged.length == 2 && merged[0] == pd && merged[1] == pd2, "append preserves order/identity");

        // recording combiner sees exact caller arrays
        final java.util.List<String> log = new java.util.ArrayList<String>();
        java.security.DomainCombiner recorder = new java.security.DomainCombiner() {
            public java.security.ProtectionDomain[] combine(java.security.ProtectionDomain[] current,
                    java.security.ProtectionDomain[] assigned) {
                log.add((current == null ? -1 : current.length) + "/" + (assigned == null ? -1 : assigned.length));
                return current;
            }
        };
        recorder.combine(new java.security.ProtectionDomain[]{pd}, null);
        recorder.combine(null, new java.security.ProtectionDomain[]{pd, pd});
        check(log.equals(java.util.Arrays.asList("1/-1", "-1/2")), "verbatim array delivery: " + log);

        // attachment: AccessControlContext(context, combiner) exposes the combiner
        java.security.AccessControlContext base =
                new java.security.AccessControlContext(new java.security.ProtectionDomain[]{pd});
        java.security.AccessControlContext combined =
                new java.security.AccessControlContext(base, identity);
        check(combined.getDomainCombiner() == identity, "getDomainCombiner retrieves attached combiner");
        check(base.getDomainCombiner() == null, "plain context has no combiner");

        // lambda implements the functional interface
        java.security.DomainCombiner lambda = (cur, asg) -> cur;
        check(lambda.combine(one, null) == one, "lambda combiner");
    }
}
