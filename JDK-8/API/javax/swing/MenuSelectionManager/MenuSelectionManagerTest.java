/*
 * @test
 * @summary javax.swing.MenuSelectionManager conformance: type shape + selection path lifecycle (headless-safe).
 * @run main MenuSelectionManagerTest
 */

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JLabel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class MenuSelectionManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.MenuSelectionManager", false,
                MenuSelectionManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"),
                "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("defaultManager"), "method defaultManager");
        check(names.contains("setSelectedPath"), "method setSelectedPath");
        check(names.contains("getSelectedPath"), "method getSelectedPath");
        check(names.contains("clearSelectedPath"), "method clearSelectedPath");
        check(names.contains("addChangeListener"), "method addChangeListener");
        check(names.contains("removeChangeListener"), "method removeChangeListener");
        check(names.contains("getChangeListeners"), "method getChangeListeners");
    }

    static class Elem implements MenuElement {
        final List<Boolean> notifications = new ArrayList<Boolean>();
        public Component getComponent() { return null; }
        public MenuElement[] getSubElements() { return new MenuElement[0]; }
        public void menuSelectionChanged(boolean isIncluded) { notifications.add(isIncluded); }
        public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager m) { }
        public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager m) { }
    }

    static void testSelectionPath() throws Exception {
        final MenuSelectionManager m = MenuSelectionManager.defaultManager();
        check(m != null, "defaultManager nonnull");
        check(m == MenuSelectionManager.defaultManager(), "defaultManager is shared");
        check(m.getSelectedPath().length == 0, "empty selection path initially");
        final Elem a = new Elem();
        final Elem b = new Elem();
        final AtomicInteger changeEvents = new AtomicInteger();
        m.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { changeEvents.incrementAndGet(); }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() { m.setSelectedPath(new MenuElement[]{a, b}); }
        });
        check(m.getSelectedPath().length == 2, "selected path size after set");
        check(m.getSelectedPath()[0] == a && m.getSelectedPath()[1] == b,
                "selected path contents preserved");
        check(a.notifications.equals(java.util.Arrays.asList(true)),
                "added element notified menuSelectionChanged(true)");
        check(b.notifications.equals(java.util.Arrays.asList(true)),
                "nested added element notified");
        check(changeEvents.get() >= 1, "selection change fires ChangeEvent");
        check(m.getChangeListeners().length == 1, "change listener registered");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() { m.clearSelectedPath(); }
        });
        check(m.getSelectedPath().length == 0, "clearSelectedPath empties path");
        check(a.notifications.equals(java.util.Arrays.asList(true, false)),
                "removed element notified menuSelectionChanged(false)");
        check(b.notifications.equals(java.util.Arrays.asList(true, false)),
                "nested removed element notified");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() { m.setSelectedPath(null); }
        });
        check(m.getSelectedPath().length == 0, "setSelectedPath(null) clears path");
        check(!m.isComponentPartOfCurrentMenu(null), "null component is never part of menu");
        check(!m.isComponentPartOfCurrentMenu(new JLabel("x")),
                "unrelated component is not part of menu");
        m.removeChangeListener(m.getChangeListeners()[0]);
        check(m.getChangeListeners().length == 0, "change listener removed");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testSelectionPath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
