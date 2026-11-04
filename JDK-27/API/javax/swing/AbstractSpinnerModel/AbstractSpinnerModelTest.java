/*
 * @test
 * @summary javax.swing.AbstractSpinnerModel conformance: white-box structure + black-box behavior.
 * @run main AbstractSpinnerModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.AbstractSpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AbstractSpinnerModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractSpinnerModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SpinnerModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AbstractSpinnerModel m = new AbstractSpinnerModel() {
            public Object getValue() { return Integer.valueOf(1); }
            public void setValue(Object v) { }
            public Object getNextValue() { return Integer.valueOf(2); }
            public Object getPreviousValue() { return Integer.valueOf(0); }
        };

        check(Integer.valueOf(1).equals(m.getValue()), "getValue");
        check(Integer.valueOf(2).equals(m.getNextValue()), "getNextValue");
        check(Integer.valueOf(0).equals(m.getPreviousValue()), "getPreviousValue");

        final int[] fired = {0};
        ChangeListener l = e -> fired[0]++;
        m.addChangeListener(l);
        check(m.getChangeListeners().length == 1, "listener registered");

        // fireStateChanged is protected; a subclass exercises it
        class FiringModel extends AbstractSpinnerModel {
            public Object getValue() { return null; }
            public void setValue(Object v) { }
            public Object getNextValue() { return null; }
            public Object getPreviousValue() { return null; }
            void fire() { fireStateChanged(); }
        }
        FiringModel firing = new FiringModel();
        firing.addChangeListener(l);
        firing.fire();
        check(fired[0] == 1, "stateChanged fired");

        check(m.getListeners(ChangeListener.class).length == 1, "getListeners count");
        m.removeChangeListener(l);
        check(m.getChangeListeners().length == 0, "listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
