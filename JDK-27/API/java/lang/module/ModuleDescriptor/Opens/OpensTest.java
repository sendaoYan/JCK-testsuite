/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Opens conformance: white-box + black-box behavior.
 * @run main OpensTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Opens;
import java.lang.module.ModuleDescriptor.Opens.Modifier;
import java.util.Set;

public class OpensTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Opens qualified() {
        return ModuleDescriptor.newModule("m")
                .opens(Set.of(Modifier.MANDATED, Modifier.SYNTHETIC), "p", Set.of("t1", "t2"))
                .build().opens().iterator().next();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Opens");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Opens is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        for (String g : new String[]{"source", "targets", "isQualified", "modifiers",
                                     "accessFlags", "compareTo", "equals", "hashCode", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(g)) found = true;
            }
            check(found, "method " + g);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Opens q = qualified();
        check("p".equals(q.source()), "source()");
        check(q.targets().equals(Set.of("t1", "t2")), "targets()");
        check(q.isQualified(), "isQualified() true");
        check(q.modifiers().equals(Set.of(Modifier.MANDATED, Modifier.SYNTHETIC)), "modifiers()");
        check(q.accessFlags() != null, "accessFlags() non-null");

        Opens uq = ModuleDescriptor.newModule("n").opens("p").build().opens().iterator().next();
        check(!uq.isQualified(), "unqualified isQualified() false");
        check(uq.targets().isEmpty(), "unqualified targets empty");
        check(uq.modifiers().isEmpty(), "unqualified modifiers empty");

        Opens q2 = qualified();
        check(q.equals(q2), "equals()");
        check(q.hashCode() == q2.hashCode(), "hashCode()");
        check(q.compareTo(q2) == 0, "compareTo() equal");
        check(!q.equals(uq), "!equals different opens");

        // compareTo orders by source package
        Opens other = ModuleDescriptor.newModule("n2").opens("a.b").build()
                .opens().iterator().next();
        check(uq.compareTo(other) > 0, "p > a.b by source");

        // different targets -> not equal
        Opens otherTargets = ModuleDescriptor.newModule("m3")
                .opens("p", Set.of("t1")).build().opens().iterator().next();
        check(!q.equals(otherTargets), "targets participate in equals");

        // open modules implicitly open everything: opens() stays empty in descriptor
        ModuleDescriptor open = ModuleDescriptor.newOpenModule("o").build();
        check(open.isOpen() && open.opens().isEmpty(), "open module has no per-package opens");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
