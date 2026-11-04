/*
 * @test
 * @summary javax.swing.SpinnerModel conformance: white-box structure + black-box behavior.
 * @run main SpinnerModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

public class SpinnerModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SpinnerModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SpinnerModel m = new SpinnerNumberModel(5, 0, 10, 1);
        check(m.getValue() != null, "getValue");
        m.setValue(5);
        check(((Number) m.getValue()).intValue() == 5, "setValue");
        check(((Number) m.getNextValue()).intValue() == 6, "getNextValue");
        check(((Number) m.getPreviousValue()).intValue() == 4, "getPreviousValue");

        ChangeListener l = e -> { };
        m.addChangeListener(l);
        m.removeChangeListener(l);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
