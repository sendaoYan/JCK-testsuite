/*
 * @test
 * @summary javax.swing.AbstractAction conformance: white-box structure + black-box behavior.
 * @run main AbstractActionTest
 */

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class AbstractActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean contains(Object[] arr, Object key) {
        if (arr == null) return false;
        for (Object o : arr) if (key.equals(o)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Action"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.lang.Cloneable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // interface key constants are the stable string keys
        check("Name".equals(Action.NAME), "Action.NAME");
        check("ShortDescription".equals(Action.SHORT_DESCRIPTION), "Action.SHORT_DESCRIPTION");
        check("LongDescription".equals(Action.LONG_DESCRIPTION), "Action.LONG_DESCRIPTION");
        check("SmallIcon".equals(Action.SMALL_ICON), "Action.SMALL_ICON");
        check("ActionCommandKey".equals(Action.ACTION_COMMAND_KEY), "Action.ACTION_COMMAND_KEY");
        check("AcceleratorKey".equals(Action.ACCELERATOR_KEY), "Action.ACCELERATOR_KEY");
        check("MnemonicKey".equals(Action.MNEMONIC_KEY), "Action.MNEMONIC_KEY");
        check("SwingSelectedKey".equals(Action.SELECTED_KEY), "Action.SELECTED_KEY");

        AbstractAction a = new AbstractAction("MyName") {
            public void actionPerformed(ActionEvent e) { }
        };
        check("MyName".equals(a.getValue(Action.NAME)), "ctor sets NAME");
        check(a.isEnabled(), "default enabled");

        // put/get round trip + key collection
        a.putValue(Action.SHORT_DESCRIPTION, "desc");
        check("desc".equals(a.getValue(Action.SHORT_DESCRIPTION)), "putValue/getValue");
        check(contains(a.getKeys(), Action.NAME), "keys contains NAME");
        check(contains(a.getKeys(), Action.SHORT_DESCRIPTION), "keys contains SHORT_DESCRIPTION");

        // enable/disable
        a.setEnabled(false);
        check(!a.isEnabled(), "disabled");
        a.setEnabled(true);
        check(a.isEnabled(), "re-enabled");

        // property change listener observes putValue
        final boolean[] fired = {false};
        PropertyChangeListener l = evt -> { if (Action.SHORT_DESCRIPTION.equals(evt.getPropertyName())) fired[0] = true; };
        a.addPropertyChangeListener(l);
        a.putValue(Action.SHORT_DESCRIPTION, "desc2");
        check(fired[0], "property change fired");
        check(a.getPropertyChangeListeners().length == 1, "listener registered");
        a.removePropertyChangeListener(l);
        check(a.getPropertyChangeListeners().length == 0, "listener removed");

        // actionPerformed is a callable no-op
        a.actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "x"));
        check(true, "actionPerformed no-op");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
