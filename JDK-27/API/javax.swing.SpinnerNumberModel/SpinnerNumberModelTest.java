/*
 * @test
 * @summary javax.swing.SpinnerNumberModel conformance: white-box structure + black-box behavior.
 * @run main SpinnerNumberModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.SpinnerNumberModel;

public class SpinnerNumberModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SpinnerNumberModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractSpinnerModel"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SpinnerNumberModel m = new SpinnerNumberModel(5, 0, 10, 1);
        check(m.getNumber().intValue() == 5, "getNumber");
        check(m.getValue().equals(Integer.valueOf(5)), "getValue");
        check(m.getMinimum().equals(Integer.valueOf(0)), "getMinimum");
        check(m.getMaximum().equals(Integer.valueOf(10)), "getMaximum");
        check(m.getStepSize().intValue() == 1, "getStepSize");
        check(m.getNextValue().equals(Integer.valueOf(6)), "getNextValue");
        check(m.getPreviousValue().equals(Integer.valueOf(4)), "getPreviousValue");

        m.setValue(11);
        check(m.getNumber().intValue() == 11, "setValue does not clamp");
        m.setMinimum(2);
        check(m.getMinimum().equals(Integer.valueOf(2)), "setMinimum");
        m.setMaximum(20);
        check(m.getMaximum().equals(Integer.valueOf(20)), "setMaximum");
        m.setStepSize(2);
        check(m.getStepSize().intValue() == 2, "setStepSize");

        SpinnerNumberModel d = new SpinnerNumberModel();
        check(d.getNumber().intValue() == 0, "default zero");
        SpinnerNumberModel f = new SpinnerNumberModel(1.5, 0.5, 2.0, 0.5);
        check(f.getNumber().doubleValue() == 1.5, "double ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
