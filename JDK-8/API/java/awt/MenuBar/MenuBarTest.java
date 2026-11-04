/*
 * @test
 * @key headful
 * @summary java.awt.MenuBar conformance: class reflection.
 * @run main MenuBarTest
 */

import java.awt.*;

public class MenuBarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractMenuBar() throws Exception {
        MenuBar mb = new MenuBar();
        check(mb.getMenuCount() == 0 && mb.countMenus() == 0, "empty menubar");
        expectExact(NullPointerException.class, "add(null)", () -> mb.add((Menu) null));
        Menu file = new Menu("File"), edit = new Menu("Edit");
        check(mb.add(file) == file && mb.add(edit) == edit, "add returns the menu");
        check(mb.getMenuCount() == 2 && mb.getMenu(0) == file && mb.getMenu(1) == edit,
              "getMenu order and countMenus");
        check(mb.countMenus() == 2, "countMenus alias");
        expectExact(ArrayIndexOutOfBoundsException.class, "getMenu(99)", () -> mb.getMenu(99));
        // help menu: replaces previous, is counted, removable via null
        mb.setHelpMenu(new Menu("H1"));
        check(mb.getHelpMenu().getLabel().equals("H1") && mb.getMenuCount() == 3,
              "help menu set and counted");
        mb.setHelpMenu(new Menu("H2"));
        check("H2".equals(mb.getHelpMenu().getLabel()) && mb.getMenuCount() == 3,
              "second help menu replaces the first");
        mb.setHelpMenu(null);
        check(mb.getHelpMenu() == null && mb.getMenuCount() == 2, "setHelpMenu(null) removes help menu");
        // removals
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(99)", () -> mb.remove(99));
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(-1)", () -> mb.remove(-1));
        mb.remove(0);
        check(mb.getMenuCount() == 1 && mb.getMenu(0) == edit, "remove(int) shifts");
        mb.remove(edit);
        check(mb.getMenuCount() == 0, "remove(MenuComponent)");
        // shortcut registry: lookup by equal shortcut, shift variants are distinct keys
        MenuBar sb = new MenuBar();
        Menu f2 = new Menu("F2");
        sb.add(f2);
        MenuItem open = new MenuItem("Open", new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O));
        f2.add(open);
        check(sb.getShortcutMenuItem(new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O)) == open,
              "shortcut lookup by equal shortcut");
        check(sb.getShortcutMenuItem(new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_N)) == null,
              "miss lookup returns null");
        java.util.Enumeration<MenuShortcut> en = sb.shortcuts();
        check(en.hasMoreElements() && en.nextElement().getKey() == java.awt.event.KeyEvent.VK_O,
              "shortcuts enumeration lists registered keys");
        sb.deleteShortcut(new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O));
        check(sb.getShortcutMenuItem(new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O)) == null,
              "deleteShortcut unregisters");
        MenuItem shifted = new MenuItem("ShiftOpen",
                new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O, true));
        f2.add(shifted);
        check(sb.getShortcutMenuItem(new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O, true)) == shifted,
              "shift-variant shortcut lookup");
        check(sb.getShortcutMenuItem(new java.awt.MenuShortcut(java.awt.event.KeyEvent.VK_O)) == null,
              "plain shortcut distinct from shift variant");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.MenuBar");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuComponent"), "superclass java.awt.MenuComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "getHelpMenu"), "method getHelpMenu");
        check(hasMethod(c, "setHelpMenu"), "method setHelpMenu");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "getMenuCount"), "method getMenuCount");
        check(hasMethod(c, "countMenus"), "method countMenus");
        check(hasMethod(c, "getMenu"), "method getMenu");
        check(hasMethod(c, "shortcuts"), "method shortcuts");
        check(hasMethod(c, "getShortcutMenuItem"), "method getShortcutMenuItem");
        check(hasMethod(c, "deleteShortcut"), "method deleteShortcut");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractMenuBar();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
