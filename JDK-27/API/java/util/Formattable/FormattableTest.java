/*
 * @test
 * @summary java.util.Formattable conformance: white-box structure + black-box behavior.
 * @run main FormattableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;

public class FormattableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Formattable that echoes the received flags/width/precision back into the formatter. */
    static class Echo implements Formattable {
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            formatter.format("[f=%d,w=%d,p=%d]", flags, width, precision);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Formattable");
        check(c.isInterface(), "is an interface");
        Method fmt = c.getDeclaredMethod("formatTo", Formatter.class, int.class, int.class, int.class);
        check(Modifier.isAbstract(fmt.getModifiers()), "formatTo abstract");
        check(fmt.getReturnType() == void.class, "formatTo returns void");
        check(fmt.getParameterTypes()[0] == Formatter.class, "first param is Formatter");
        check(fmt.getParameterTypes()[1] == int.class, "flags param int");
        check(fmt.getParameterTypes()[2] == int.class, "width param int");
        check(fmt.getParameterTypes()[3] == int.class, "precision param int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. formatTo writes directly to the Formatter
        StringBuilder sb = new StringBuilder();
        new Echo().formatTo(new Formatter(sb), 0, -1, -1);
        check("[f=0,w=-1,p=-1]".equals(sb.toString()), "formatTo writes to the formatter");

        // 2. %s passes flags=0, width=-1, precision=-1
        check("[f=0,w=-1,p=-1]".equals(String.format("%s", new Echo())), "%s default flags/width/precision");

        // 3. width and precision are passed through
        check("[f=1,w=10,p=4]".equals(String.format("%-10.4s", new Echo())),
                "%-10.4s passes LEFT_JUSTIFY + width + precision");

        // 4. %S sets UPPERCASE; %#S sets UPPERCASE|ALTERNATE
        check("[f=2,w=-1,p=-1]".equals(String.format("%S", new Echo())), "%S sets UPPERCASE");
        check("[f=6,w=-1,p=-1]".equals(String.format("%#S", new Echo())),
                "%#S sets UPPERCASE|ALTERNATE");

        // 5. flag constants relationship
        check(FormattableFlags.LEFT_JUSTIFY == 1, "LEFT_JUSTIFY == 1");
        check(FormattableFlags.UPPERCASE == 2, "UPPERCASE == 2");
        check(FormattableFlags.ALTERNATE == 4, "ALTERNATE == 4");
        check((FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE
                | FormattableFlags.ALTERNATE) == 7, "flags are bitmask combinable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
