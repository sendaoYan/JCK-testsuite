/*
 * @test
 * @summary javax.swing.JMenuItem conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JMenuItemTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyListener;

public class JMenuItemTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class FixedIcon implements Icon {
        final int w, h;
        FixedIcon(int w, int h) { this.w = w; this.h = h; }
        public int getIconWidth() { return w; }
        public int getIconHeight() { return h; }
        public void paintIcon(Component c, Graphics g, int x, int y) {}
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JMenuItem");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.MenuElement"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructors
        JMenuItem dflt = new JMenuItem();
        check("".equals(dflt.getText()), "default JMenuItem text is empty string");
        JMenuItem cut = new JMenuItem("Cut");
        check("Cut".equals(cut.getText()), "JMenuItem(String) stores text");
        FixedIcon icon = new FixedIcon(16, 16);
        FixedIcon icon2 = new FixedIcon(24, 24);
        JMenuItem copy = new JMenuItem("Copy", icon);
        check("Copy".equals(copy.getText()), "JMenuItem(String,Icon) stores text");
        check(copy.getIcon() == icon, "JMenuItem(String,Icon) stores icon");
        check("MenuItemUI".equals(cut.getUIClassID()), "UIClassID is MenuItemUI");

        // text/icon/actionCommand round-trips
        cut.setText("Cut X");
        check("Cut X".equals(cut.getText()), "setText reflected");
        cut.setIcon(icon2);
        check(cut.getIcon() == icon2, "setIcon reflected");
        check("Cut X".equals(cut.getActionCommand()), "actionCommand falls back to current text");
        cut.setActionCommand("cut-cmd");
        check("cut-cmd".equals(cut.getActionCommand()), "setActionCommand reflected");

        // enabled/armed/mnemonic/accelerator round-trips
        check(cut.isEnabled(), "enabled defaults true");
        cut.setEnabled(false);
        check(!cut.isEnabled(), "setEnabled(false) reflected");
        cut.setEnabled(true);
        check(cut.isEnabled(), "setEnabled(true) reflected");
        check(!cut.isArmed(), "armed defaults false");
        cut.setArmed(true);
        check(cut.isArmed(), "setArmed(true) reflected");
        cut.setArmed(false);
        check(!cut.isArmed(), "setArmed(false) reflected");
        check(cut.getMnemonic() == 0, "mnemonic defaults 0");
        cut.setMnemonic('F');
        check(cut.getMnemonic() == 'F', "setMnemonic reflected");
        check(cut.getAccelerator() == null, "accelerator defaults null");
        KeyStroke ks = KeyStroke.getKeyStroke('C');
        cut.setAccelerator(ks);
        check(ks.equals(cut.getAccelerator()), "setAccelerator reflected");

        // doClick fires an ActionEvent with the button as source + actionCommand
        final List<ActionEvent> events = new ArrayList<ActionEvent>();
        ActionListener listener = e -> events.add(e);
        cut.addActionListener(listener);
        cut.doClick(0);
        check(events.size() == 1, "doClick fires exactly one action event");
        check(events.get(0).getSource() == cut, "action event source is the menu item");
        check("cut-cmd".equals(events.get(0).getActionCommand()), "action event carries actionCommand");
        check(events.get(0).getID() == ActionEvent.ACTION_PERFORMED, "action event id is ACTION_PERFORMED");

        // disabled menu item does not fire action on doClick
        cut.setEnabled(false);
        cut.doClick(0);
        check(events.size() == 1, "disabled doClick does not fire action");
        cut.setEnabled(true);

        // Action constructor copies NAME into text
        Action act = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {}
        };
        act.putValue(Action.NAME, "FromAction");
        JMenuItem fromAction = new JMenuItem(act);
        check("FromAction".equals(fromAction.getText()), "JMenuItem(Action) copies NAME to text");

        // MenuElement plumbing
        check(cut.getSubElements().length == 0, "JMenuItem has no sub-elements");
        check(cut.getComponent() == cut, "MenuElement.getComponent() returns the item");

        // menu key / drag-mouse listener round-trips (baseline-relative; UI may install one)
        int keyBase = cut.getMenuKeyListeners().length;
        MenuKeyListener mkl = new MenuKeyListener() {
            public void menuKeyTyped(javax.swing.event.MenuKeyEvent e) {}
            public void menuKeyPressed(javax.swing.event.MenuKeyEvent e) {}
            public void menuKeyReleased(javax.swing.event.MenuKeyEvent e) {}
        };
        cut.addMenuKeyListener(mkl);
        check(cut.getMenuKeyListeners().length == keyBase + 1, "addMenuKeyListener reflected");
        cut.removeMenuKeyListener(mkl);
        check(cut.getMenuKeyListeners().length == keyBase, "removeMenuKeyListener reflected");

        int dragBase = cut.getMenuDragMouseListeners().length;
        MenuDragMouseListener mdl = new MenuDragMouseListener() {
            public void menuDragMouseEntered(javax.swing.event.MenuDragMouseEvent e) {}
            public void menuDragMouseExited(javax.swing.event.MenuDragMouseEvent e) {}
            public void menuDragMouseDragged(javax.swing.event.MenuDragMouseEvent e) {}
            public void menuDragMouseReleased(javax.swing.event.MenuDragMouseEvent e) {}
        };
        cut.addMenuDragMouseListener(mdl);
        check(cut.getMenuDragMouseListeners().length == dragBase + 1, "addMenuDragMouseListener reflected");
        cut.removeMenuDragMouseListener(mdl);
        check(cut.getMenuDragMouseListeners().length == dragBase, "removeMenuDragMouseListener reflected");

        // menuSelectionChanged is a no-op that must not throw
        cut.menuSelectionChanged(true);
        check(true, "menuSelectionChanged(true) completes");
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

    public static void main(String[] args) throws Exception {
        runOnEDT(JMenuItemTest::testWhiteBox);
        runOnEDT(JMenuItemTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
