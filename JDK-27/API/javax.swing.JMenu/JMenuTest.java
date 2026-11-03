/*
 * @test
 * @summary javax.swing.JMenu conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JMenuTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Modifier;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.MenuListener;

public class JMenuTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JMenu");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JMenuItem"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.MenuElement"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructors + label/delay/UIClassID
        JMenu empty = new JMenu();
        check("".equals(empty.getText()), "default JMenu text is empty string");
        JMenu file = new JMenu("File");
        check("File".equals(file.getText()), "JMenu(String) stores text");
        JMenu edit = new JMenu("Edit", false);
        check("Edit".equals(edit.getText()), "JMenu(String,boolean) stores text");
        check("MenuUI".equals(file.getUIClassID()), "UIClassID is MenuUI");
        check(file.getDelay() == 200, "default menu delay is 200ms");
        file.setDelay(350);
        check(file.getDelay() == 350, "setDelay reflected");
        file.setDelay(0);
        check(file.getDelay() == 0, "setDelay(0) reflected");
        boolean negDelayThrew = false;
        try { file.setDelay(-1); } catch (IllegalArgumentException e) { negDelayThrew = true; }
        check(negDelayThrew, "setDelay(-1) throws IllegalArgumentException");

        // add(String)/add(JMenuItem)/addSeparator/insert chain
        check(file.getItemCount() == 0, "new menu has 0 items");
        JMenuItem open = file.add("Open");
        check(open instanceof JMenuItem, "add(String) returns JMenuItem");
        check("Open".equals(open.getText()), "add(String) item has text");
        check(file.getItemCount() == 1, "item count 1 after add(String)");
        check(file.getItem(0) == open, "getItem(0) returns same added item");
        JMenuItem save = file.add(new JMenuItem("Save"));
        check(file.getItemCount() == 2, "item count 2 after add(JMenuItem)");
        check(file.getItem(1) == save, "getItem(1) returns added JMenuItem");
        file.addSeparator();
        check(file.getItemCount() == 3, "addSeparator increments item count");
        file.insert("New", 0);
        check(file.getItemCount() == 4, "insert(String,0) increments count");
        check("New".equals(file.getItem(0).getText()), "insert at head places item first");
        file.insertSeparator(1);
        check(file.getItemCount() == 5, "insertSeparator increments count");
        check(file.getMenuComponentCount() == 5, "getMenuComponentCount equals item count");
        check(file.getMenuComponents().length == 5, "getMenuComponents length equals item count");
        check(file.getMenuComponent(0) == file.getItem(0), "getMenuComponent(0) matches getItem(0)");
        check("Open".equals(file.getItem(2).getText()), "item order preserved after inserts");
        check("Save".equals(file.getItem(3).getText()), "item order preserved after inserts");

        // remove by reference / by index / removeAll
        file.remove(open);
        check(file.getItemCount() == 4, "remove(JMenuItem) decrements count");
        file.remove(0);
        check(file.getItemCount() == 3, "remove(int) decrements count");
        file.removeAll();
        check(file.getItemCount() == 0, "removeAll clears items");

        // isMenuComponent / getPopupMenu
        JMenuItem item = new JMenuItem("Item");
        check(!file.isMenuComponent(item), "isMenuComponent false for unrelated item");
        file.add(item);
        check(file.isMenuComponent(item), "isMenuComponent true for added item");
        check(file.getPopupMenu() != null, "getPopupMenu returns non-null popup");
        check(file.getPopupMenu() == file.getPopupMenu(), "getPopupMenu returns same instance");

        // isTopLevelMenu depends on JMenuBar parent
        check(!file.isTopLevelMenu(), "menu without menubar parent is not top-level");
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        check(file.isTopLevelMenu(), "menu inside a JMenuBar is top-level");

        // accelerator / tear-off are unsupported on JMenu -> Error
        boolean accelThrew = false;
        try { file.setAccelerator(KeyStroke.getKeyStroke('A')); } catch (Error e) { accelThrew = true; }
        check(accelThrew, "JMenu.setAccelerator throws Error");
        boolean tearOffThrew = false;
        try { file.isTearOff(); } catch (Error e) { tearOffThrew = true; }
        check(tearOffThrew, "JMenu.isTearOff throws Error");

        // doClick drives the MenuSelectionManager selection path
        JMenu menu = new JMenu("Click");
        JMenuBar bar2 = new JMenuBar();
        bar2.add(menu);
        menu.doClick(0);
        MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
        check(path != null && path.length == 3, "doClick sets a menubar-menu-popup selection path");
        check(path[0] == bar2, "selection path head is the menubar");
        check(path[1] == menu, "selection path middle is the menu");
        check(path[2] == menu.getPopupMenu(), "selection path tail is the menu popup");
        MenuSelectionManager.defaultManager().clearSelectedPath();

        // popup visibility default / no-op hide
        check(!menu.isPopupMenuVisible(), "popup not visible by default");
        menu.setPopupMenuVisible(false);
        check(!menu.isPopupMenuVisible(), "setPopupMenuVisible(false) keeps hidden");

        // menu listener round-trip (baseline-relative; UI may install one)
        int menuListenerBase = menu.getMenuListeners().length;
        MenuListener l = new MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent e) {}
            public void menuDeselected(javax.swing.event.MenuEvent e) {}
            public void menuCanceled(javax.swing.event.MenuEvent e) {}
        };
        menu.addMenuListener(l);
        check(menu.getMenuListeners().length == menuListenerBase + 1, "addMenuListener reflected");
        menu.removeMenuListener(l);
        check(menu.getMenuListeners().length == menuListenerBase, "removeMenuListener reflected");
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
        runOnEDT(JMenuTest::testWhiteBox);
        runOnEDT(JMenuTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
