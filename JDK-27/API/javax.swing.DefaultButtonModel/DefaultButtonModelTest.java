/*
 * @test
 * @summary javax.swing.DefaultButtonModel conformance: white-box structure + black-box behavior.
 * @run main DefaultButtonModelTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultButtonModel;

public class DefaultButtonModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultButtonModel");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.ButtonModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultButtonModel m = new DefaultButtonModel();

        // initial state
        check(!m.isArmed() && !m.isSelected() && m.isEnabled() && !m.isPressed() && !m.isRollover(), "default state");

        // state setters round-trip
        m.setArmed(true);
        check(m.isArmed(), "setArmed");
        m.setPressed(true);
        check(m.isPressed(), "setPressed");
        m.setRollover(true);
        check(m.isRollover(), "setRollover");
        m.setSelected(true);
        check(m.isSelected(), "setSelected");

        // action command + mnemonic
        m.setActionCommand("run");
        check("run".equals(m.getActionCommand()), "setActionCommand");
        m.setMnemonic('A');
        check(m.getMnemonic() == 'A', "setMnemonic");

        // disabled clears transient armed/pressed state
        m.setEnabled(false);
        check(!m.isEnabled(), "setEnabled(false)");
        check(!m.isArmed(), "disable clears armed");
        check(!m.isPressed(), "disable clears pressed");

        // armed + press/release fires an action event
        DefaultButtonModel fire = new DefaultButtonModel();
        final int[] actions = {0};
        fire.addActionListener(e -> actions[0]++);
        fire.setArmed(true);
        fire.setPressed(true);
        fire.setPressed(false);
        check(actions[0] == 1, "action fired on press/release");
        check(fire.getActionListeners().length == 1, "action listener count");

        // selected toggling fires an item event
        final int[] items = {0};
        fire.addItemListener(e -> items[0]++);
        fire.setSelected(true);
        check(items[0] == 1, "item fired on select");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
