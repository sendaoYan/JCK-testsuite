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
