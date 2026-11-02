/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Modifier conformance: OPEN, AUTOMATIC, SYNTHETIC and MANDATED enum constants.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.OPEN != null, "OPEN present");
        check(Modifier.AUTOMATIC != null, "AUTOMATIC present");
        check(Modifier.SYNTHETIC != null, "SYNTHETIC present");
        check(Modifier.MANDATED != null, "MANDATED present");

        Modifier[] all = Modifier.values();
        check(all.length == 4, "values() length 4");
        check(all[0] == Modifier.OPEN && all[1] == Modifier.AUTOMATIC
                && all[2] == Modifier.SYNTHETIC && all[3] == Modifier.MANDATED,
              "values() order OPEN, AUTOMATIC, SYNTHETIC, MANDATED");

        check(Modifier.valueOf("OPEN") == Modifier.OPEN, "valueOf(OPEN)");
        check(Modifier.valueOf("MANDATED") == Modifier.MANDATED, "valueOf(MANDATED)");
        check("AUTOMATIC".equals(Modifier.AUTOMATIC.name()), "name()");
        check(Modifier.OPEN.ordinal() == 0, "ordinal()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
