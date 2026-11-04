/*
 * @test
 * @summary javax.sound.sampled.FloatControl conformance: white-box structure + black-box behavior.
 * @run main FloatControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.FloatControl;

public class FloatControlTest {
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
        Class<?> c = Class.forName("javax.sound.sampled.FloatControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");

        Class<?> t = Class.forName("javax.sound.sampled.FloatControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FloatControl fc = new FloatControl(FloatControl.Type.MASTER_GAIN,
                -80f, 6f, 0.1f, 1000, 0f, "dB", "Min", "Mid", "Max") {};

        check(fc.getType() == FloatControl.Type.MASTER_GAIN, "type MASTER_GAIN");
        check(fc.getMinimum() == -80f, "minimum");
        check(fc.getMaximum() == 6f, "maximum");
        check(fc.getPrecision() == 0.1f, "precision");
        check(fc.getUpdatePeriod() == 1000, "updatePeriod");
        check("dB".equals(fc.getUnits()), "units");
        check("Min".equals(fc.getMinLabel()), "minLabel");
        check("Mid".equals(fc.getMidLabel()), "midLabel");
        check("Max".equals(fc.getMaxLabel()), "maxLabel");
        check(fc.getValue() == 0f, "initial value");

        fc.setValue(3.5f);
        check(fc.getValue() == 3.5f, "setValue round-trip");
        fc.setValue(-80f);
        check(fc.getValue() == -80f, "setValue at minimum");
        fc.setValue(6f);
        check(fc.getValue() == 6f, "setValue at maximum");

        expectThrows(IllegalArgumentException.class, () -> fc.setValue(6.1f), "setValue above maximum");
        expectThrows(IllegalArgumentException.class, () -> fc.setValue(-80.1f), "setValue below minimum");

        // shift validates 'from' and applies 'to' via setValue
        fc.shift(0f, -20f, 5000);
        check(fc.getValue() == -20f, "shift sets value to 'to'");
        expectThrows(IllegalArgumentException.class, () -> fc.shift(-200f, 0f, 1000), "shift 'from' below minimum");
        expectThrows(IllegalArgumentException.class, () -> fc.shift(0f, 200f, 1000), "shift 'to' above maximum");

        // 6-arg constructor gives empty min/mid/max labels
        FloatControl fc2 = new FloatControl(FloatControl.Type.VOLUME, 0f, 1f, 0.01f, -1, 0.5f, "") {};
        check("".equals(fc2.getMinLabel()), "empty minLabel");
        check("".equals(fc2.getMidLabel()), "empty midLabel");
        check("".equals(fc2.getMaxLabel()), "empty maxLabel");
        check(fc2.getUpdatePeriod() == -1, "updatePeriod -1 (no shifting)");
        check(fc2.getValue() == 0.5f, "initial value 0.5");

        check(fc.toString().contains("dB"), "toString has units");

        // constructor validation: minimum > maximum
        expectThrows(IllegalArgumentException.class,
                () -> new FloatControl(FloatControl.Type.PAN, 10f, -10f, 1f, 100, 0f, "u") {},
                "min > max rejected");
        // constructor validation: initial value out of range
        expectThrows(IllegalArgumentException.class,
                () -> new FloatControl(FloatControl.Type.PAN, -1f, 1f, 0.1f, 100, 2f, "u") {},
                "initial value above range rejected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
