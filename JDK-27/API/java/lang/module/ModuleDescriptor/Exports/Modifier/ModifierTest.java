/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Exports.Modifier conformance: white-box + black-box behavior.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports.Modifier;
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
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Exports$Modifier");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 2, "two constants");
        check(c.getDeclaringClass() == java.lang.module.ModuleDescriptor.Exports.class,
              "nested in Exports");
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
            Modifier.valueOf("OPEN");
            check(false, "unknown rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown IAE");
        }

        check(Modifier.SYNTHETIC.ordinal() == 0, "SYNTHETIC 0");
        check(Modifier.MANDATED.ordinal() == 1, "MANDATED 1");
        check(Modifier.SYNTHETIC.compareTo(Modifier.MANDATED) < 0, "compareTo");

        // round trip through Builder/Exports for each constant
        for (Modifier m : Modifier.values()) {
            ModuleDescriptor.Exports e = ModuleDescriptor.newModule("t" + m.ordinal())
                    .exports(Set.of(m), "p.q").build().exports().iterator().next();
            check(e.modifiers().equals(Set.of(m)), "round trip " + m);
        }

        // mandated exports appear in automatic module descriptors derived by ModuleFinder
        // (not constructible here); plain exports carry no modifiers
        ModuleDescriptor.Exports plain = ModuleDescriptor.newModule("plain")
                .exports("p.r").build().exports().iterator().next();
        check(plain.modifiers().isEmpty(), "plain export has empty modifiers");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
