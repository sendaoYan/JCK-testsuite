/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Requires.Modifier conformance: TRANSITIVE, STATIC, SYNTHETIC and MANDATED enum constants.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor.Requires.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.TRANSITIVE != null, "TRANSITIVE present");
        check(Modifier.STATIC != null, "STATIC present");
        check(Modifier.SYNTHETIC != null, "SYNTHETIC present");
        check(Modifier.MANDATED != null, "MANDATED present");

        Modifier[] all = Modifier.values();
        check(all.length == 4, "values() length 4");
        check(all[0] == Modifier.TRANSITIVE && all[1] == Modifier.STATIC
                && all[2] == Modifier.SYNTHETIC && all[3] == Modifier.MANDATED,
              "values() order TRANSITIVE, STATIC, SYNTHETIC, MANDATED");

        check(Modifier.valueOf("TRANSITIVE") == Modifier.TRANSITIVE, "valueOf(TRANSITIVE)");
        check(Modifier.valueOf("STATIC") == Modifier.STATIC, "valueOf(STATIC)");
        check("MANDATED".equals(Modifier.MANDATED.name()), "name()");
        check(Modifier.TRANSITIVE.ordinal() == 0, "ordinal()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
