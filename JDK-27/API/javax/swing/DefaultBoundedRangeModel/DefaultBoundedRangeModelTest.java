/*
 * @test
 * @summary javax.swing.DefaultBoundedRangeModel conformance: white-box structure + black-box behavior.
 * @run main DefaultBoundedRangeModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeListener;

public class DefaultBoundedRangeModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultBoundedRangeModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.BoundedRangeModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel();
        check(m.getValue() == 0 && m.getExtent() == 0 && m.getMinimum() == 0 && m.getMaximum() == 100, "defaults");

        DefaultBoundedRangeModel m2 = new DefaultBoundedRangeModel(50, 10, 0, 100);
        check(m2.getValue() == 50 && m2.getExtent() == 10 && m2.getMinimum() == 0 && m2.getMaximum() == 100, "4-arg ctor");

        m2.setValue(75);
        check(m2.getValue() == 75, "setValue");
        m2.setExtent(20);
        check(m2.getExtent() == 20, "setExtent");
        m2.setMinimum(5);
        check(m2.getMinimum() == 5, "setMinimum");
        m2.setMaximum(90);
        check(m2.getMaximum() == 90, "setMaximum");

        check(!m2.getValueIsAdjusting(), "default not adjusting");
        m2.setValueIsAdjusting(true);
        check(m2.getValueIsAdjusting(), "setValueIsAdjusting");

        final int[] fired = {0};
        ChangeListener l = e -> fired[0]++;
        m2.addChangeListener(l);
        m2.setRangeProperties(10, 5, 0, 100, false);
        check(m2.getValue() == 10 && m2.getExtent() == 5 && m2.getMinimum() == 0 && m2.getMaximum() == 100, "setRangeProperties");
        check(fired[0] >= 1, "state changed fired");
        check(m2.getChangeListeners().length == 1, "listener count");
        m2.removeChangeListener(l);
        check(m2.getChangeListeners().length == 0, "listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
