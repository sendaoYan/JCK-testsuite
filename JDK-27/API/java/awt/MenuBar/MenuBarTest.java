/*
 * @test
 * @summary java.awt.MenuBar conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MenuBarTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class MenuBarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.MenuBar");
        check(c != null, "type loads: java.awt.MenuBar");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == MenuComponent.class, "extends MenuComponent");
        boolean menuContainer = false;
        for (Class<?> i : c.getInterfaces()) if (i == MenuContainer.class) menuContainer = true;
        check(menuContainer, "implements MenuContainer");
        for (String name : new String[]{"getHelpMenu", "setHelpMenu", "add", "remove",
                "getMenuCount", "getMenu", "getShortcutMenuItem", "deleteShortcut", "shortcuts"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MenuBar mb = new MenuBar();
        check(mb.getMenuCount() == 0, "initial empty");
        check(mb.getHelpMenu() == null, "no help menu initially");

        Menu file = new Menu("File");
        check(mb.add(file) == file, "add returns menu");
        check(mb.getMenuCount() == 1, "getMenuCount");
        check(mb.getMenu(0) == file, "getMenu(0)");

        Menu help = new Menu("Help");
        mb.setHelpMenu(help);
        check(mb.getHelpMenu() == help, "setHelpMenu/getHelpMenu");

        // shortcut lookup
        MenuItem open = new MenuItem("Open", new MenuShortcut(79));
        file.add(open);
        check(mb.getShortcutMenuItem(new MenuShortcut(79)) == open, "getShortcutMenuItem");
        mb.deleteShortcut(new MenuShortcut(79));
        check(mb.getShortcutMenuItem(new MenuShortcut(79)) == null, "deleteShortcut");

        // shortcuts enumeration
        MenuItem save = new MenuItem("Save", new MenuShortcut(83));
        file.add(save);
        boolean found = false;
        java.util.Enumeration<MenuShortcut> en = mb.shortcuts();
        while (en.hasMoreElements()) {
            if (en.nextElement().getKey() == 83) found = true;
        }
        check(found, "shortcuts enumeration contains key");

        // remove tests on a fresh MenuBar (setHelpMenu also appends the help menu)
        MenuBar mb2 = new MenuBar();
        Menu m1 = new Menu("M1");
        Menu m2 = new Menu("M2");
        mb2.add(m1);
        mb2.add(m2);
        check(mb2.getMenuCount() == 2, "two menus added");
        mb2.remove(0);
        check(mb2.getMenuCount() == 1 && mb2.getMenu(0) == m2, "remove(int)");
        mb2.remove(m2);
        check(mb2.getMenuCount() == 0, "remove(MenuComponent)");
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
        testWhiteBox();
        testBlackBox();
        testContractMenuBar();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
