/*
 * @test
 * @summary javax.swing.Action conformance: key constants, contract shape, adapter via AbstractAction (headless-safe).
 * @run main ActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;

public class ActionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- existing shape assertions (preserved) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.Action", false, ActionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.awt.event.ActionListener"), "implements java.awt.event.ActionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "method getValue");
        check(names.contains("putValue"), "method putValue");
        check(names.contains("setEnabled"), "method setEnabled");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("addPropertyChangeListener"), "method addPropertyChangeListener");
        check(names.contains("removePropertyChangeListener"), "method removePropertyChangeListener");
    }

    // ---- key string constants contract ----
    static void testKeyConstants() {
        check("Default".equals(Action.DEFAULT), "DEFAULT key value");
        check("Name".equals(Action.NAME), "NAME key value");
        check("ShortDescription".equals(Action.SHORT_DESCRIPTION), "SHORT_DESCRIPTION key value");
        check("LongDescription".equals(Action.LONG_DESCRIPTION), "LONG_DESCRIPTION key value");
        check("SmallIcon".equals(Action.SMALL_ICON), "SMALL_ICON key value");
        check("ActionCommandKey".equals(Action.ACTION_COMMAND_KEY), "ACTION_COMMAND_KEY key value");
        check("AcceleratorKey".equals(Action.ACCELERATOR_KEY), "ACCELERATOR_KEY key value");
        check("MnemonicKey".equals(Action.MNEMONIC_KEY), "MNEMONIC_KEY key value");
        check("SwingDisplayedMnemonicIndexKey".equals(Action.DISPLAYED_MNEMONIC_INDEX_KEY), "DISPLAYED_MNEMONIC_INDEX_KEY key value");
        check("SwingLargeIconKey".equals(Action.LARGE_ICON_KEY), "LARGE_ICON_KEY key value");
        check("SwingSelectedKey".equals(Action.SELECTED_KEY), "SELECTED_KEY key value");
        // all keys distinct
        Object[] keys = {Action.DEFAULT, Action.NAME, Action.SHORT_DESCRIPTION, Action.LONG_DESCRIPTION,
                Action.SMALL_ICON, Action.ACTION_COMMAND_KEY, Action.ACCELERATOR_KEY,
                Action.MNEMONIC_KEY, Action.DISPLAYED_MNEMONIC_INDEX_KEY, Action.LARGE_ICON_KEY, Action.SELECTED_KEY};
        Set<Object> uniq = new HashSet<Object>();
        for (Object k : keys) check(uniq.add(k), "key constants unique: " + k);
    }

    // ---- contract behavior through a minimal implementor ----
    static void testContractViaAbstractAction() {
        final int[] fired = new int[1];
        Action a = new AbstractAction("probe") {
            public void actionPerformed(ActionEvent e) { fired[0]++; }
        };
        check(a.isEnabled(), "Action default enabled");
        a.setEnabled(false);
        check(!a.isEnabled(), "setEnabled(false)");
        a.setEnabled(true);
        check(a.isEnabled(), "setEnabled(true)");
        check("probe".equals(a.getValue(Action.NAME)), "getValue(NAME)");
        a.putValue(Action.SHORT_DESCRIPTION, "tip");
        check("tip".equals(a.getValue(Action.SHORT_DESCRIPTION)), "putValue/getValue roundtrip");
        check(a.getValue("absent") == null, "getValue absent key -> null");
        a.putValue("custom", "x");
        check("x".equals(a.getValue("custom")), "arbitrary key accepted");
        a.putValue("custom", null);
        check(a.getValue("custom") == null, "putValue(key,null) -> null read");
        // ActionListener contract: Action is an ActionListener
        a.actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "cmd"));
        check(fired[0] == 1, "actionPerformed dispatched to implementor");
        java.awt.event.ActionListener al = (java.awt.event.ActionListener) a;
        al.actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "cmd"));
        check(fired[0] == 2, "cast to ActionListener works");
        // property change notification contract
        final String[] got = new String[1];
        java.beans.PropertyChangeListener pcl = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) { got[0] = e.getPropertyName(); }
        };
        a.addPropertyChangeListener(pcl);
        a.setEnabled(false);
        check("enabled".equals(got[0]), "enabled property change fired");
        a.putValue(Action.NAME, "n2");
        check(Action.NAME.equals(got[0]), "putValue(NAME) fires property change under key NAME");
        a.removePropertyChangeListener(pcl);
        got[0] = null;
        a.setEnabled(true);
        check(got[0] == null, "removed listener not notified");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testKeyConstants();
        testContractViaAbstractAction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
