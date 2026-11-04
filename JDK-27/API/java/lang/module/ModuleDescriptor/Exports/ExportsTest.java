/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Exports conformance: white-box + black-box behavior.
 * @run main ExportsTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Exports.Modifier;
import java.util.Set;

public class ExportsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Exports qualified() {
        return ModuleDescriptor.newModule("m")
                .exports(Set.of(Modifier.MANDATED, Modifier.SYNTHETIC), "p", Set.of("t1", "t2"))
                .build().exports().iterator().next();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Exports");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Exports is final");
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
        Exports q = qualified();
        check("p".equals(q.source()), "source()");
        check(q.targets().equals(Set.of("t1", "t2")), "targets()");
        check(q.isQualified(), "isQualified() true");
        check(q.modifiers().equals(Set.of(Modifier.MANDATED, Modifier.SYNTHETIC)), "modifiers()");
        check(q.accessFlags() != null, "accessFlags() non-null");

        Exports uq = ModuleDescriptor.newModule("n").exports("p").build().exports().iterator().next();
        check(!uq.isQualified(), "unqualified isQualified() false");
        check(uq.targets().isEmpty(), "unqualified targets empty");
        check(uq.modifiers().isEmpty(), "unqualified modifiers empty");

        // value semantics
        Exports q2 = qualified();
        check(q.equals(q2), "equals()");
        check(q.hashCode() == q2.hashCode(), "hashCode()");
        check(q.compareTo(q2) == 0, "compareTo() equal");
        check(!q.equals(uq), "!equals different exports");
        check(q.toString() != null, "toString() non-null");

        // compareTo orders by source package
        Exports other = ModuleDescriptor.newModule("n2").exports("a.b").build()
                .exports().iterator().next();
        check(uq.compareTo(other) > 0, "p > a.b by source");

        // different targets -> not equal
        Exports otherTargets = ModuleDescriptor.newModule("m3")
                .exports("p", Set.of("t1")).build().exports().iterator().next();
        check(!q.equals(otherTargets), "targets participate in equals");

        // single-target qualified export round trip
        Exports single = ModuleDescriptor.newModule("m4")
                .exports("x.y", Set.of("friend")).build().exports().iterator().next();
        check(single.isQualified() && single.targets().equals(Set.of("friend")),
              "single qualified target");

        // real-world: java.base exports java.lang unqualified
        ModuleDescriptor base = ModuleLayer.boot()
                .findModule("java.base").orElseThrow().getDescriptor();
        check(base.exports().stream().anyMatch(
                      e -> e.source().equals("java.lang") && !e.isQualified()),
              "java.base unqualified export of java.lang");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
