/*
 * @test
 * @summary javax.swing.JButton conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JButtonTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Icon fixedIcon() {
        return new Icon() {
            public int getIconWidth() { return 8; }
            public int getIconHeight() { return 8; }
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) { }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        // supplemented structural checks
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(String.class) != null, "ctor String");
        check(c.getConstructor(Icon.class) != null, "ctor Icon");
        check(c.getConstructor(Action.class) != null, "ctor Action");
        check(Class.forName("javax.swing.JButton$AccessibleJButton").getSuperclass().getName()
                .equals("javax.swing.AbstractButton$AccessibleAbstractButton"), "AccessibleJButton nesting");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JButton button = new JButton();
        check(button.getText().equals(""), "default text empty");
        check(button.getModel() != null, "default model non-null");
        button.setText("Press");
        check(button.getText().equals("Press"), "setText/getText round trip");
        button.setMnemonic('P');
        check(button.getMnemonic() == (int) 'P', "setMnemonic(char) stores keycode");
        button.setMnemonic(KeyEvent.VK_F4);
        check(button.getMnemonic() == KeyEvent.VK_F4, "setMnemonic(int) round trip");
        button.setActionCommand("fire");
        check(button.getActionCommand().equals("fire"), "actionCommand round trip");

        Icon icon = fixedIcon();
        JButton iconButton = new JButton(icon);
        check(iconButton.getIcon() == icon, "ctor Icon identity");

        // setAction binding: text/enabled follow the Action, doClick drives actionPerformed
        AbstractAction action = new AbstractAction("Go") {
            public void actionPerformed(ActionEvent e) { }
        };
        JButton bound = new JButton();
        final int[] actionCount = { 0 };
        bound.setAction(action);
        check(bound.getAction() == action, "getAction same instance");
        check(bound.getText().equals("Go"), "setAction syncs NAME to text");
        check(bound.isEnabled() == action.isEnabled(), "setAction syncs enabled");
        action.setEnabled(false);
        check(!bound.isEnabled(), "action disabled disables button");
        action.setEnabled(true);
        check(bound.isEnabled(), "action re-enabled enables button");
        action.putValue(Action.NAME, "Stop");
        check(bound.getText().equals("Stop"), "NAME property change updates text");

        // doClick fires ActionListener + model ChangeEvents; no ItemEvent for a plain button
        final List<String> commands = new ArrayList<>();
        bound.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commands.add(e.getActionCommand());
                actionCount[0]++;
            }
        });
        final int[] changeCount = { 0 };
        bound.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { changeCount[0]++; }
        });
        final int[] itemCount = { 0 };
        bound.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) { itemCount[0]++; }
        });
        bound.doClick();
        check(actionCount[0] == 1, "doClick triggers action.actionPerformed once");
        check(commands.size() == 1, "one ActionEvent delivered");
        check(commands.get(0).equals("Stop"), "ActionEvent command follows action NAME");
        check(changeCount[0] == 4, "doClick drives armed/pressed model transitions (4 ChangeEvents)");
        check(itemCount[0] == 0, "plain button doClick fires no ItemEvent");

        // disabled button ignores doClick
        bound.setEnabled(false);
        bound.doClick();
        check(actionCount[0] == 1, "doClick on disabled button fires nothing");

        // setModel round trip: shared DefaultButtonModel, no group
        DefaultButtonModel model = new DefaultButtonModel();
        button.setModel(model);
        check(button.getModel() == model, "setModel/getModel same instance");
        check(model.getGroup() == null, "plain button model has no group");
        model.setActionCommand("model-cmd");
        check(button.getActionCommand().equals("model-cmd"), "actionCommand delegated to model");

        // rollover / defaultCapable round trips
        button.setRolloverEnabled(false);
        check(!button.isRolloverEnabled(), "rollover disabled round trip");
        button.setRolloverEnabled(true);
        check(button.isRolloverEnabled(), "rollover enabled round trip");
        check(button.isDefaultCapable(), "defaultCapable default true");
        button.setDefaultCapable(false);
        check(!button.isDefaultCapable(), "defaultCapable false round trip");

        // root pane default button chain
        JFrame frame = new JFrame("JButtonTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(button);
            frame.pack();
            frame.setVisible(true);
            check(frame.isVisible(), "frame visible");
            JRootPane root = frame.getRootPane();
            root.setDefaultButton(button);
            check(root.getDefaultButton() == button, "setDefaultButton/getDefaultButton same instance");
            check(button.isDefaultButton(), "isDefaultButton true while installed");
            root.setDefaultButton(null);
            check(root.getDefaultButton() == null, "defaultButton cleared");
            check(!button.isDefaultButton(), "isDefaultButton false after clear");
        } finally {
            frame.dispose();
        }
        check(!frame.isVisible(), "frame not visible after dispose");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JButtonTest::testWhiteBox);
        runOnEDT(JButtonTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
