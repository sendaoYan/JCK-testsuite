/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Exports conformance: source(), targets(), isQualified(), modifiers(), equality.
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

    public static void main(String[] args) {
        Exports q = qualified();
        check("p".equals(q.source()), "source()");
        check(q.targets().equals(Set.of("t1", "t2")), "targets()");
        check(q.isQualified(), "isQualified() true");
        check(q.modifiers().equals(Set.of(Modifier.MANDATED, Modifier.SYNTHETIC)), "modifiers()");

        Exports uq = ModuleDescriptor.newModule("n").exports("p").build().exports().iterator().next();
        check(!uq.isQualified(), "unqualified isQualified() false");
        check(uq.targets().isEmpty(), "unqualified targets empty");
        check(uq.modifiers().isEmpty(), "unqualified modifiers empty");

        Exports q2 = qualified();
        check(q.equals(q2), "equals()");
        check(q.hashCode() == q2.hashCode(), "hashCode()");
        check(q.compareTo(q2) == 0, "compareTo() equal");
        check(!q.equals(uq), "!equals different exports");
        check(q.toString() != null, "toString() non-null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
