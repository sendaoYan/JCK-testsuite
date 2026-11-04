/*
 * @test
 * @summary java.security.DomainCombiner conformance: white-box structure + black-box behavior.
 * @run main DomainCombinerTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class DomainCombinerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DomainCombiner");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"combine"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Functional interface: implement combine(current, assigned) and attach via AccessControlContext
        List<String> log = new ArrayList<>();
        DomainCombiner combiner = (current, assigned) -> {
            log.add("combine: current=" + (current == null ? -1 : current.length)
                + " assigned=" + (assigned == null ? -1 : assigned.length));
            ProtectionDomain[] out = new ProtectionDomain[(current == null ? 0 : current.length) + (assigned == null ? 0 : assigned.length)];
            int i = 0;
            if (current != null) for (ProtectionDomain d : current) out[i++] = d;
            if (assigned != null) for (ProtectionDomain d : assigned) out[i++] = d;
            return out;
        };
        ProtectionDomain pd = new ProtectionDomain(null, null);
        AccessControlContext base = new AccessControlContext(new ProtectionDomain[]{pd});
        AccessControlContext combined = new AccessControlContext(base, combiner);
        check(combined.getDomainCombiner() == combiner, "combiner attached");
        // identity combiner keeps domains untouched
        DomainCombiner identity = (c, a) -> c;
        check(identity.combine(new ProtectionDomain[]{pd}, null).length == 1, "identity combiner");
        check(identity.combine(null, null) == null, "identity null");
        // a filtering combiner drops everything
        DomainCombiner dropper = (c, a) -> new ProtectionDomain[0];
        check(dropper.combine(new ProtectionDomain[]{pd, pd}, new ProtectionDomain[]{pd}).length == 0, "dropper combiner");
        // an appending combiner concatenates
        DomainCombiner appender = (c, a) -> {
            ProtectionDomain[] out = new ProtectionDomain[(c == null ? 0 : c.length) + (a == null ? 0 : a.length)];
            int i = 0;
            if (c != null) for (ProtectionDomain d : c) out[i++] = d;
            if (a != null) for (ProtectionDomain d : a) out[i++] = d;
            return out;
        };
        check(appender.combine(new ProtectionDomain[]{pd}, new ProtectionDomain[]{pd}).length == 2, "appender");
        // DomainCombiner is a functional interface (single abstract method)
        check(DomainCombiner.class.getMethod("combine", ProtectionDomain[].class, ProtectionDomain[].class) != null,
            "single combine method");

        // ---- appended contract dimensions ----
        // exact signature: combine returns ProtectionDomain[], not default, single declared method
        Method[] ms0 = DomainCombiner.class.getDeclaredMethods();
        check(ms0.length == 1, "single declared method");
        check(ms0[0].getReturnType() == ProtectionDomain[].class, "returns ProtectionDomain[]");
        check(!ms0[0].isDefault(), "not a default method");
        check(ms0[0].getParameterCount() == 2, "two parameters");
        // identity combiner preserves array identity; null inputs tolerated
        ProtectionDomain[] one = {pd};
        check(identity.combine(one, null) == one, "identity returns caller array");
        check(identity.combine(null, null) == null, "combine(null,null) tolerated");
        // append preserves element order and identity
        ProtectionDomain pd2 = new ProtectionDomain(null, null);
        ProtectionDomain[] merged = appender.combine(new ProtectionDomain[]{pd}, new ProtectionDomain[]{pd2});
        check(merged.length == 2 && merged[0] == pd && merged[1] == pd2, "append order/identity");
        // recorder sees exact caller arrays
        final List<String> log2 = new ArrayList<>();
        DomainCombiner recorder = (cur, asg) -> {
            log2.add((cur == null ? -1 : cur.length) + "/" + (asg == null ? -1 : asg.length));
            return cur;
        };
        recorder.combine(new ProtectionDomain[]{pd}, null);
        recorder.combine(null, new ProtectionDomain[]{pd, pd});
        check(log2.equals(Arrays.asList("1/-1", "-1/2")), "verbatim array delivery: " + log2);
        // attachment retrieval: plain context has no combiner
        check(base.getDomainCombiner() == null, "plain context no combiner");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
