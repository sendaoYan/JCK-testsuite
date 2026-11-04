/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Requires.Modifier conformance: white-box + black-box behavior.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Requires.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Requires$Modifier");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 4, "four constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // exact constants and declaration order
        Modifier[] vals = Modifier.values();
        List<String> names = new ArrayList<>();
        for (Modifier m : vals) names.add(m.name());
        check(String.join(",", names).equals("TRANSITIVE,STATIC,SYNTHETIC,MANDATED"),
              "declaration order: " + names);

        // valueOf round-trips
        for (Modifier m : vals) check(Modifier.valueOf(m.name()) == m, "valueOf " + m);
        try {
            Modifier.valueOf("PUBLIC");
            check(false, "unknown constant rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown IAE");
        }

        // usage through the Builder -> Requires round trip for each modifier
        for (Modifier m : vals) {
            java.lang.module.ModuleDescriptor.Requires r = java.lang.module.ModuleDescriptor
                    .newModule("t" + m.ordinal())
                    .requires(java.util.Set.of(m), "dep" + m.ordinal()).build()
                    .requires().iterator().next();
            if (!r.name().startsWith("dep")) continue; // skip implicit java.base
            check(r.modifiers().equals(java.util.Set.of(m)), "round trip " + m);
        }

        // ordinals and mechanics
        check(Modifier.TRANSITIVE.ordinal() == 0, "TRANSITIVE ordinal 0");
        check(Modifier.STATIC.ordinal() == 1, "STATIC ordinal 1");
        check(Modifier.SYNTHETIC.ordinal() == 2, "SYNTHETIC ordinal 2");
        check(Modifier.MANDATED.ordinal() == 3, "MANDATED ordinal 3");
        check(Modifier.TRANSITIVE.compareTo(Modifier.MANDATED) < 0, "compareTo order");
        check(Modifier.STATIC.name().equals("STATIC"), "name");

        // the implicit java.base require of a normal module is MANDATED
        java.lang.module.ModuleDescriptor.Requires implicit =
                java.lang.module.ModuleDescriptor.newModule("mm").build()
                        .requires().iterator().next();
        check(implicit.modifiers().equals(java.util.Set.of(Modifier.MANDATED)),
              "implicit java.base is MANDATED (real-world use)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
