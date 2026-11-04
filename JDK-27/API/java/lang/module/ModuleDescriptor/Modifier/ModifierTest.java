/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Modifier conformance: white-box + black-box behavior.
 * @run main ModifierTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Modifier;
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
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Modifier");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 4, "four constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constants and order
        List<String> names = new ArrayList<>();
        for (Modifier m : Modifier.values()) names.add(m.name());
        check(String.join(",", names).equals("OPEN,AUTOMATIC,SYNTHETIC,MANDATED"),
              "declaration order: " + names);

        // valueOf round-trips and rejection
        for (Modifier m : Modifier.values()) check(Modifier.valueOf(m.name()) == m, "valueOf " + m);
        try {
            Modifier.valueOf("TRANSITIVE");
            check(false, "unknown constant rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown IAE");
        }

        // ordinals
        check(Modifier.OPEN.ordinal() == 0, "OPEN 0");
        check(Modifier.AUTOMATIC.ordinal() == 1, "AUTOMATIC 1");
        check(Modifier.SYNTHETIC.ordinal() == 2, "SYNTHETIC 2");
        check(Modifier.MANDATED.ordinal() == 3, "MANDATED 3");

        // newModule(name, Set<Modifier>) drives descriptor.modifiers()
        ModuleDescriptor open = ModuleDescriptor.newModule("mo", Set.of(Modifier.OPEN)).build();
        check(open.modifiers().equals(Set.of(Modifier.OPEN)), "OPEN modifier round trip");
        check(open.isOpen(), "OPEN modifier implies isOpen()");
        ModuleDescriptor synth = ModuleDescriptor.newModule("ms", Set.of(Modifier.SYNTHETIC)).build();
        check(synth.modifiers().equals(Set.of(Modifier.SYNTHETIC)), "SYNTHETIC round trip");
        check(!synth.isOpen() && !synth.isAutomatic(), "SYNTHETIC alone is neither open nor auto");

        // automatic factory reports AUTOMATIC
        check(ModuleDescriptor.newAutomaticModule("ma").build().modifiers()
                      .equals(Set.of(Modifier.AUTOMATIC)),
              "newAutomaticModule -> AUTOMATIC");
        // plain module has none
        check(ModuleDescriptor.newModule("mp").build().modifiers().isEmpty(),
              "plain module has no modifiers");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
