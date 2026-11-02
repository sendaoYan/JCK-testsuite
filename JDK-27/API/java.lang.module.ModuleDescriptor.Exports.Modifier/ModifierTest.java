/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Exports.Modifier conformance: SYNTHETIC and MANDATED enum constants.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor.Exports.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.SYNTHETIC != null, "SYNTHETIC present");
        check(Modifier.MANDATED != null, "MANDATED present");

        Modifier[] all = Modifier.values();
        check(all.length == 2, "values() length 2");
        check(all[0] == Modifier.SYNTHETIC && all[1] == Modifier.MANDATED, "values() order SYNTHETIC, MANDATED");

        check(Modifier.valueOf("SYNTHETIC") == Modifier.SYNTHETIC, "valueOf(SYNTHETIC)");
        check(Modifier.valueOf("MANDATED") == Modifier.MANDATED, "valueOf(MANDATED)");
        check("SYNTHETIC".equals(Modifier.SYNTHETIC.name()), "name()");
        check(Modifier.SYNTHETIC.ordinal() == 0, "ordinal()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
