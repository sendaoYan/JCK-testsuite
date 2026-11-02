/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Opens conformance: source(), targets(), isQualified(), modifiers(), equality.
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

    public static void main(String[] args) {
        Opens q = qualified();
        check("p".equals(q.source()), "source()");
        check(q.targets().equals(Set.of("t1", "t2")), "targets()");
        check(q.isQualified(), "isQualified() true");
        check(q.modifiers().equals(Set.of(Modifier.MANDATED, Modifier.SYNTHETIC)), "modifiers()");

        Opens uq = ModuleDescriptor.newModule("n").opens("p").build().opens().iterator().next();
        check(!uq.isQualified(), "unqualified isQualified() false");
        check(uq.targets().isEmpty(), "unqualified targets empty");
        check(uq.modifiers().isEmpty(), "unqualified modifiers empty");

        Opens q2 = qualified();
        check(q.equals(q2), "equals()");
        check(q.hashCode() == q2.hashCode(), "hashCode()");
        check(q.compareTo(q2) == 0, "compareTo() equal");
        check(!q.equals(uq), "!equals different opens");
        check(q.toString() != null, "toString() non-null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
