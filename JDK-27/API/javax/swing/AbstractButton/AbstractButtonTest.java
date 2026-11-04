/*
 * @test
 * @summary javax.swing.AbstractButton conformance: white-box structure + black-box behavior.
 * @run main AbstractButtonTest
 */

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

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

    // ---- Part 3: deep contract under Metal (mirrors the JDK-8 side) ----
    static class CountingIcon implements Icon {
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) { }
        public int getIconWidth() { return 5; }
        public int getIconHeight() { return 3; }
    }

    static class RecordingAction extends AbstractAction {
        int count;
        List<String> commands = new ArrayList<>();
        RecordingAction(String name) { super(name); }
        public void actionPerformed(ActionEvent e) { count++; commands.add(e.getActionCommand()); }
    }

    static void testContract() throws Exception {
        JButton b = new JButton("t");
        check("t".equals(b.getText()), "text ctor");
        check("t".equals(b.getActionCommand()), "implicit actionCommand follows text");

        // ---- Metal defaults for a plain button
        check(b.getHorizontalAlignment() == SwingConstants.CENTER, "horizontalAlignment default CENTER");
        check(b.getVerticalAlignment() == SwingConstants.CENTER, "verticalAlignment default CENTER");
        check(b.getHorizontalTextPosition() == SwingConstants.TRAILING, "horizontalTextPosition default TRAILING");
        check(b.getVerticalTextPosition() == SwingConstants.CENTER, "verticalTextPosition default CENTER");
        check(b.getIconTextGap() == 4, "iconTextGap default 4");
        Insets m = b.getMargin();
        check(m != null && m.top == 2 && m.left == 14 && m.bottom == 2 && m.right == 14,
                "Metal margin [2,14,2,14]");
        check(b.isRolloverEnabled(), "rolloverEnabled default true");
        check(b.isBorderPainted(), "borderPainted default true");
        check(b.isFocusPainted(), "focusPainted default true");
        check(b.getModel() instanceof DefaultButtonModel, "DefaultButtonModel installed");
        check(b.getChangeListeners().length >= 1, "change listeners wired to model");
        check(b.getDisplayedMnemonicIndex() == -1, "displayedMnemonicIndex default -1");
        check(b.getMultiClickThreshhold() == 0, "multiClickThreshhold default 0");

        // ---- text / actionCommand interplay
        b.setText(null);
        check(b.getText() == null, "setText(null) -> null (not empty string)");
        b.setText("AC");
        check("AC".equals(b.getActionCommand()), "implicit actionCommand tracks setText");
        b.setActionCommand("cmd");
        check("cmd".equals(b.getActionCommand()), "explicit actionCommand wins");

        // ---- doClick fires ActionEvents with the current action command
        List<String> cmds = new ArrayList<>();
        long[] whenHolder = new long[1];
        b.addActionListener(e -> { cmds.add(e.getActionCommand()); whenHolder[0] = e.getWhen(); });
        b.doClick();
        check(cmds.size() == 1 && "cmd".equals(cmds.get(0)), "doClick fires ActionEvent with explicit command");
        check(whenHolder[0] > 0, "ActionEvent timestamp positive");
        b.doClick(50);
        check(cmds.size() == 2, "doClick(ms) fires too");
        b.setActionCommand(null);
        b.doClick();
        check(cmds.size() == 3 && "AC".equals(cmds.get(2)), "unset actionCommand falls back to current text");

        // ---- alignment setters reject illegal values
        try { new JButton("x").setHorizontalAlignment(99); check(false, "IAE hAlign"); }
        catch (IllegalArgumentException expected) { passed++; }
        try { new JButton("x").setVerticalAlignment(99); check(false, "IAE vAlign"); }
        catch (IllegalArgumentException expected) { passed++; }
        try { new JButton("x").setHorizontalTextPosition(99); check(false, "IAE hTextPos"); }
        catch (IllegalArgumentException expected) { passed++; }
        try { new JButton("x").setVerticalTextPosition(99); check(false, "IAE vTextPos"); }
        catch (IllegalArgumentException expected) { passed++; }

        // ---- buttons tolerate a negative iconTextGap ( JLabel rejects alignment 99 but not this )
        b.setIconTextGap(-1);
        check(b.getIconTextGap() == -1, "setIconTextGap(-1) accepted on buttons");
        b.setMargin(new Insets(1, 2, 3, 4));
        check(b.getMargin().equals(new Insets(1, 2, 3, 4)), "setMargin round trip");

        // ---- mnemonic plumbing and property event name
        List<String> pNames = new ArrayList<>();
        b.addPropertyChangeListener(e -> pNames.add(e.getPropertyName()));
        b.setMnemonic('A');
        check(b.getMnemonic() == 'A', "setMnemonic(char)");
        check(pNames.contains("mnemonic"), "setMnemonic fires 'mnemonic'");
        b.setMnemonic(0);
        check(b.getMnemonic() == 0, "setMnemonic(0) clears");
        b.setMnemonic('A');
        b.setDisplayedMnemonicIndex(-1);
        check(b.getDisplayedMnemonicIndex() == -1, "setDisplayedMnemonicIndex(-1)");
        try { new JButton("xy").setDisplayedMnemonicIndex(99); check(false, "IAE mnemonicIndex"); }
        catch (IllegalArgumentException expected) { passed++; }

        // ---- icon setters are plain identity storage
        CountingIcon ic = new CountingIcon();
        b.setIcon(ic);                 check(b.getIcon() == ic, "setIcon");
        b.setPressedIcon(ic);          check(b.getPressedIcon() == ic, "setPressedIcon");
        b.setDisabledIcon(ic);         check(b.getDisabledIcon() == ic, "setDisabledIcon");
        b.setRolloverIcon(ic);         check(b.getRolloverIcon() == ic, "setRolloverIcon");
        b.setSelectedIcon(ic);         check(b.getSelectedIcon() == ic, "setSelectedIcon");
        b.setRolloverSelectedIcon(ic); check(b.getRolloverSelectedIcon() == ic, "setRolloverSelectedIcon");
        b.setRolloverEnabled(true);
        check(b.isRolloverEnabled(), "setRolloverEnabled(true)");

        // ---- disabled icon derivation: only ImageIcon instances get one, on query; explicit wins
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(4, 3, java.awt.image.BufferedImage.TYPE_INT_RGB);
        ImageIcon imgIcon = new ImageIcon(img);
        JButton db = new JButton("d", imgIcon);
        Icon derived = db.getDisabledIcon();
        check(derived != null && derived != imgIcon, "ImageIcon gets a LaF-derived disabledIcon");
        check(derived.getIconWidth() == 4 && derived.getIconHeight() == 3,
                "derived disabledIcon keeps source dimensions");
        JButton dn = new JButton("d2", ic);
        check(dn.getDisabledIcon() == null, "plain Icon implementation gets no derived disabledIcon");
        Icon explicit = new ImageIcon(img);
        db.setDisabledIcon(explicit);
        check(db.getDisabledIcon() == explicit, "explicit setDisabledIcon beats derivation");

        // ---- low-level ButtonModel state machine
        javax.swing.ButtonModel mod = b.getModel();
        mod.setArmed(true);
        mod.setPressed(true);
        check(mod.isArmed() && mod.isPressed(), "armed+pressed recorded");
        check(!mod.isSelected(), "DefaultButtonModel stays unselected through press");
        mod.setPressed(false);
        check(!mod.isSelected(), "DefaultButtonModel release does not latch selection");
        // a toggle model latches on release
        JToggleButton tb = new JToggleButton("t2");
        JToggleButton.ToggleButtonModel tmod = (JToggleButton.ToggleButtonModel) tb.getModel();
        List<ItemEvent> items = new ArrayList<>();
        tb.addItemListener((ItemListener) e -> items.add(e));
        tmod.setArmed(true);
        tmod.setPressed(true);
        check(!tmod.isSelected(), "toggle unselected while pressed");
        tmod.setPressed(false);
        check(tmod.isSelected(), "ToggleButtonModel release latches selection");
        tmod.setPressed(true);
        tmod.setPressed(false);
        check(!tmod.isSelected(), "second release cycle unselects");
        check(items.size() == 2, "ItemEvents for both transitions");
        check(items.get(0).getStateChange() == ItemEvent.SELECTED
                && items.get(1).getStateChange() == ItemEvent.DESELECTED,
                "ItemEvent SELECTED then DESELECTED");
        check(items.get(0).getItem() == tb, "JToggleButton re-fires ItemEvent with the button as item");
        check(items.get(0).getItemSelectable() == tb, "ItemEvent source is the button");
        items.clear();
        tb.setSelected(true);
        check(tb.isSelected() && items.size() == 1, "setSelected fires one ItemEvent");

        // ---- setModel swaps the delegate and re-homes the handler listeners
        int beforeHandlers = b.getActionListeners().length;
        boolean[] changedFired = { false };
        ChangeListener cl = e -> changedFired[0] = true;
        b.addChangeListener(cl);
        DefaultButtonModel m2 = new DefaultButtonModel();
        b.setModel(m2);
        check(b.getModel() == m2, "setModel identity");
        check(!changedFired[0], "setModel itself fires no ChangeEvent");
        check(b.getActionListeners().length == beforeHandlers, "action listeners preserved across setModel");
        check(m2.getActionListeners().length == 1, "handler listener moved onto new model");
        b.removeChangeListener(cl);

        // ---- ButtonGroup exclusivity and removal semantics
        JToggleButton t1 = new JToggleButton("t1"), t3 = new JToggleButton("t3");
        ButtonGroup g = new ButtonGroup();
        g.add(tb); g.add(t1); g.add(t3);
        tb.setSelected(true);
        t1.setSelected(true);
        check(!tb.isSelected() && t1.isSelected(), "setSelected inside a group switches selection exclusively");
        t3.doClick();
        check(!t1.isSelected() && t3.isSelected(), "doClick flips exclusivity inside group");
        check(g.getSelection() == t3.getModel(), "getSelection is the selected button's model");
        java.util.Enumeration<AbstractButton> en = g.getElements();
        int seen = 0;
        while (en.hasMoreElements()) { en.nextElement(); seen++; }
        check(g.getButtonCount() == 3 && seen == 3, "group membership count");
        g.remove(t3);
        check(g.getSelection() == null, "removing selected member clears getSelection");
        check(t3.isSelected(), "removed button keeps its own selection state");
        t3.setSelected(false);
        tb.setSelected(true);
        check(tb.isSelected() && !t3.isSelected(), "removed member now independent");
        g.clearSelection();
        check(!tb.isSelected() && g.getSelection() == null, "clearSelection deselects all");

        // ---- setAction full sync + live updates + reset
        RecordingAction act = new RecordingAction("actName");
        act.putValue(Action.SHORT_DESCRIPTION, "tipX");
        act.putValue(Action.MNEMONIC_KEY, Integer.valueOf('A'));
        List<String> actNames = new ArrayList<>();
        b.addPropertyChangeListener(e -> actNames.add(e.getPropertyName()));
        b.setAction(act);
        check("actName".equals(b.getText()), "setAction syncs text");
        check("tipX".equals(b.getToolTipText()), "setAction syncs tooltip");
        check(b.getMnemonic() == 'A', "setAction syncs mnemonic");
        check(b.getAction() == act, "getAction identity");
        act.putValue(Action.NAME, "renamed");
        check("renamed".equals(b.getText()), "later Action.NAME changes keep syncing");
        act.putValue(Action.SHORT_DESCRIPTION, "tip2");
        check("tip2".equals(b.getToolTipText()), "later SHORT_DESCRIPTION changes keep syncing");
        act.setEnabled(false);
        check(!b.isEnabled(), "Action.enabled false disables button");
        act.setEnabled(true);
        check(b.isEnabled(), "Action.enabled true re-enables button");
        check(actNames.contains("Action") || actNames.contains("action"), "setAction fires action property change");
        b.doClick();
        check(act.count == 1, "doClick forwards to Action.actionPerformed");
        b.setAction(null);
        check(b.getText() == null, "setAction(null) clears text");
        check(b.isEnabled(), "setAction(null) re-enables");
        check(b.getAction() == null, "setAction(null) clears action");
        // hideActionText keeps the label blank while an action is installed
        b.setAction(act);
        b.setHideActionText(true);
        check(b.getText() == null, "hideActionText suppresses text sync");
        b.setHideActionText(false);
        check("renamed".equals(b.getText()), "unhiding resyncs text");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(() -> {
            try {
                testWhiteBox();
                testBlackBox();
                testContract();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
