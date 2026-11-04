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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
