/*
 * @test
 * @summary javax.swing.AbstractButton conformance: white-box structure + black-box behavior.
 * @run main AbstractButtonTest
 */

import java.awt.Insets;
import java.lang.reflect.Modifier;

import javax.swing.AbstractButton;
import javax.swing.DefaultButtonModel;

public class AbstractButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.ItemSelectable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.SwingConstants"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // the no-arg ctor leaves model null; a real subclass installs one
        AbstractButton b = new AbstractButton() {};
        check(b.getModel() == null, "model starts null");
        b.setModel(new DefaultButtonModel());
        check(b.getModel() != null, "setModel installs model");

        // text + mnemonic + action command
        b.setText("hello");
        check("hello".equals(b.getText()), "setText/getText");
        b.setMnemonic('A');
        check(b.getMnemonic() == 'A', "setMnemonic(char)");
        b.setMnemonic(java.awt.event.KeyEvent.VK_X);
        check(b.getMnemonic() == java.awt.event.KeyEvent.VK_X, "setMnemonic(int)");
        b.setActionCommand("run");
        check("run".equals(b.getActionCommand()), "setActionCommand");

        // selection state
        check(!b.isSelected(), "default not selected");
        b.setSelected(true);
        check(b.isSelected(), "setSelected(true)");

        // margin round trip
        b.setMargin(new Insets(1, 2, 3, 4));
        check(new Insets(1, 2, 3, 4).equals(b.getMargin()), "setMargin/getMargin");

        // visual flags
        check(b.isBorderPainted(), "default border painted");
        b.setBorderPainted(false);
        check(!b.isBorderPainted(), "setBorderPainted(false)");
        check(b.isContentAreaFilled(), "default content filled");
        b.setContentAreaFilled(false);
        check(!b.isContentAreaFilled(), "setContentAreaFilled(false)");
        b.setRolloverEnabled(true);
        check(b.isRolloverEnabled(), "setRolloverEnabled(true)");

        // displayed mnemonic index
        b.setDisplayedMnemonicIndex(0);
        check(b.getDisplayedMnemonicIndex() == 0, "displayed mnemonic index");

        // doClick fires an ActionEvent on the installed model/listener
        final int[] clicks = {0};
        b.addActionListener(e -> clicks[0]++);
        b.doClick();
        check(clicks[0] == 1, "doClick fires action once");
        check(b.getActionListeners().length == 1, "action listener registered");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
