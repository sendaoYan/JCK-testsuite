/*
 * @test
 * @summary java.util.FormattableFlags conformance: white-box structure + black-box behavior.
 * @run main FormattableFlagsTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.FormattableFlags;

public class FormattableFlagsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.FormattableFlags");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPrivate(ctors[0].getModifiers()),
                "single private constructor (utility class)");
        for (String f : new String[] {"LEFT_JUSTIFY", "UPPERCASE", "ALTERNATE"}) {
            Field field = c.getField(f);
            check(Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers()), f + " public static final");
            check(field.getType() == int.class, f + " is int");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. exact constant values (specified bitmask values)
        check(FormattableFlags.LEFT_JUSTIFY == 1, "LEFT_JUSTIFY == 1");
        check(FormattableFlags.UPPERCASE == 2, "UPPERCASE == 2");
        check(FormattableFlags.ALTERNATE == 4, "ALTERNATE == 4");

        // 2. flags are distinct powers of two (bitmask-combinable)
        check((FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE
                | FormattableFlags.ALTERNATE) == 7, "bitwise OR of all three is 7");
        check((FormattableFlags.LEFT_JUSTIFY & FormattableFlags.UPPERCASE) == 0,
                "flags do not overlap");

        // 3. used as the flag argument passed to Formattable.formatTo
        class Probe implements java.util.Formattable {
            int flags;
            public void formatTo(java.util.Formatter formatter, int flags, int width, int precision) {
                this.flags = flags;
            }
        }
        Probe p = new Probe();
        String.format("%#S", p);
        check(p.flags == (FormattableFlags.UPPERCASE | FormattableFlags.ALTERNATE),
                "flags passed to formatTo match FormattableFlags bitmask");
    }


    // ===================== 契约深化：位值组合/Formattable 回调对齐 =====================
    static void testFlagsContracts() {
        check(FormattableFlags.LEFT_JUSTIFY == 1 && FormattableFlags.UPPERCASE == 2
              && FormattableFlags.ALTERNATE == 4, "flag bit values 1/2/4");
        check((FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE
              | FormattableFlags.ALTERNATE) == 7, "bitmask-combinable");
        final int[] got = { -1 };
        class F implements java.util.Formattable {
            public void formatTo(java.util.Formatter f, int flags, int width, int precision) {
                got[0] = flags;
                f.format("x");
            }
        }
        String.format("%#-4S", new F());
        check(got[0] == 7, "%#-4S delivers all three flags (LEFT|UPPER|ALT)");
        String.format("%s", new F());
        check(got[0] == 0, "plain %s delivers no flags");
        String.format("%-10.2S", new F());
        check(got[0] == (FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE),
              "%-10.2S delivers LEFT_JUSTIFY|UPPERCASE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFlagsContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
