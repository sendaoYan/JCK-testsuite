/*
 * @test
 * @summary javax.swing.Action conformance: white-box structure + black-box behavior.
 * @run main ActionTest
 */

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;

import javax.swing.Action;

public class ActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Action");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.ActionListener"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check("Default".equals(Action.DEFAULT), "DEFAULT");
        check("Name".equals(Action.NAME), "NAME");
        check("ShortDescription".equals(Action.SHORT_DESCRIPTION), "SHORT_DESCRIPTION");
        check("LongDescription".equals(Action.LONG_DESCRIPTION), "LONG_DESCRIPTION");
        check("SmallIcon".equals(Action.SMALL_ICON), "SMALL_ICON");
        check("ActionCommandKey".equals(Action.ACTION_COMMAND_KEY), "ACTION_COMMAND_KEY");
        check("AcceleratorKey".equals(Action.ACCELERATOR_KEY), "ACCELERATOR_KEY");
        check("MnemonicKey".equals(Action.MNEMONIC_KEY), "MNEMONIC_KEY");
        check("SwingSelectedKey".equals(Action.SELECTED_KEY), "SELECTED_KEY");
        check("SwingDisplayedMnemonicIndexKey".equals(Action.DISPLAYED_MNEMONIC_INDEX_KEY), "DISPLAYED_MNEMONIC_INDEX_KEY");
        check("SwingLargeIconKey".equals(Action.LARGE_ICON_KEY), "LARGE_ICON_KEY");

        Action a = new Action() {
            private Object v;
            private boolean enabled = true;
            public Object getValue(String key) { return key.equals(Action.NAME) ? v : null; }
            public void putValue(String key, Object value) { if (key.equals(Action.NAME)) v = value; }
            public void setEnabled(boolean b) { enabled = b; }
            public boolean isEnabled() { return enabled; }
            public void addPropertyChangeListener(PropertyChangeListener l) { }
            public void removePropertyChangeListener(PropertyChangeListener l) { }
            public void actionPerformed(ActionEvent e) { }
        };

        // default method accept() returns true
        check(a.accept(new Object()), "accept default true");

        a.putValue(Action.NAME, "n");
        check("n".equals(a.getValue(Action.NAME)), "put/get value");
        check(a.isEnabled(), "default enabled");
        a.setEnabled(false);
        check(!a.isEnabled(), "disabled");
        a.actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "x"));
        check(true, "actionPerformed callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
