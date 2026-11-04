/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Opens.Modifier conformance: white-box + black-box behavior.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Opens.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Opens$Modifier");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 2, "two constants");
        check(c.getDeclaringClass() == java.lang.module.ModuleDescriptor.Opens.class,
              "nested in Opens");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<String> names = new ArrayList<>();
        for (Modifier m : Modifier.values()) names.add(m.name());
        check(String.join(",", names).equals("SYNTHETIC,MANDATED"),
              "declaration order: " + names);

        check(Modifier.valueOf("SYNTHETIC") == Modifier.SYNTHETIC, "valueOf SYNTHETIC");
        check(Modifier.valueOf("MANDATED") == Modifier.MANDATED, "valueOf MANDATED");
        try {
            Modifier.valueOf("TRANSITIVE");
            check(false, "unknown rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown IAE");
        }

        check(Modifier.SYNTHETIC.ordinal() == 0, "SYNTHETIC 0");
        check(Modifier.MANDATED.ordinal() == 1, "MANDATED 1");
        check(Modifier.SYNTHETIC.compareTo(Modifier.MANDATED) < 0, "compareTo");

        // round trip through Builder/Opens for each constant
        for (Modifier m : Modifier.values()) {
            ModuleDescriptor.Opens o = ModuleDescriptor.newModule("t" + m.ordinal())
                    .opens(Set.of(m), "p.q").build().opens().iterator().next();
            check(o.modifiers().equals(Set.of(m)), "round trip " + m);
        }

        // plain opens carry no modifiers
        ModuleDescriptor.Opens plain = ModuleDescriptor.newModule("plain")
                .opens("p.r").build().opens().iterator().next();
        check(plain.modifiers().isEmpty(), "plain open has empty modifiers");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
