/*
 * @test
 * @summary javax.sound.sampled.EnumControl conformance: white-box structure + black-box behavior.
 * @run main EnumControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.EnumControl;

public class EnumControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.EnumControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");

        Class<?> t = Class.forName("javax.sound.sampled.EnumControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object[] values = { "small hall", "large hall", "cathedral" };
        EnumControl ec = new EnumControl(EnumControl.Type.REVERB, values, "large hall") {};

        check(ec.getType() == EnumControl.Type.REVERB, "type REVERB");
        check("large hall".equals(ec.getValue()), "initial value");

        Object[] got = ec.getValues();
        check(got.length == 3, "values length 3");
        check(got != values, "getValues returns a clone");
        check("small hall".equals(got[0]) && "large hall".equals(got[1]) && "cathedral".equals(got[2]), "values order preserved");

        ec.setValue("cathedral");
        check("cathedral".equals(ec.getValue()), "setValue round-trip");
        ec.setValue("small hall");
        check("small hall".equals(ec.getValue()), "setValue back");

        check(ec.toString().contains("small hall"), "toString has current value");

        expectThrows(IllegalArgumentException.class, () -> ec.setValue("nope"), "setValue unsupported");
        check("small hall".equals(ec.getValue()), "value unchanged after failed set");

        // mutating the returned array must not affect the control
        got = ec.getValues();
        got[0] = "corrupted";
        check("small hall".equals(ec.getValues()[0]), "internal values protected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
