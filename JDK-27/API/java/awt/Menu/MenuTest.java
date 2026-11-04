/*
 * @test
 * @summary java.awt.Menu conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MenuTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class MenuTest {
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
        Class<?> c = Class.forName("java.awt.Menu");
        check(c != null, "type loads: java.awt.Menu");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == MenuItem.class, "extends MenuItem");
        boolean menuContainer = false;
        for (Class<?> i : c.getInterfaces()) if (i == MenuContainer.class) menuContainer = true;
        check(menuContainer, "implements MenuContainer");
        for (String name : new String[]{"isTearOff", "getItemCount", "getItem", "add",
                "insert", "addSeparator", "insertSeparator", "remove", "removeAll"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Menu m = new Menu();
        check("".equals(m.getLabel()), "default empty label");
        check(!m.isTearOff(), "not tear-off default");

        Menu m2 = new Menu("File");
        check("File".equals(m2.getLabel()), "constructor label");
        Menu m3 = new Menu("Edit", true);
        check(m3.isTearOff(), "constructor tear-off true");

        MenuItem a = new MenuItem("a");
        check(m2.add(a) == a, "add(MenuItem) returns item");
        m2.add("b"); // adds a MenuItem with label
        m2.addSeparator();
        check(m2.getItemCount() == 3, "add/addSeparator count");
        check(m2.getItem(0) == a, "getItem(0)");
        check("b".equals(m2.getItem(1).getLabel()), "add(String) creates item");
        check(m2.getItem(2).getLabel().equals("-"), "separator label");

        MenuItem c = new MenuItem("c");
        m2.insert(c, 1);
        check(m2.getItemCount() == 4 && m2.getItem(1) == c, "insert at index");
        m2.insertSeparator(2);
        check(m2.getItem(2).getLabel().equals("-"), "insertSeparator");

        m2.remove(1);
        check(m2.getItemCount() == 4 && m2.getItem(1).getLabel().equals("-"), "remove(int)");
        m2.remove(a);
        check(m2.getItemCount() == 3, "remove(MenuComponent)");
        m2.removeAll();
        check(m2.getItemCount() == 0, "removeAll");

        // inherited MenuItem label/enabled
        m2.setLabel("View");
        check("View".equals(m2.getLabel()), "inherited setLabel");
        m2.setEnabled(false);
        check(!m2.isEnabled(), "inherited setEnabled");
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
    static void testContractMenu() throws Exception {
        Menu m = new Menu();
        check("".equals(m.getLabel()), "Menu() default empty label");
        check(new Menu(null).getLabel() == null, "Menu(null) retains null");
        check(new Menu("T", true).isTearOff() && !new Menu("T").isTearOff(), "tear-off ctor flag");
        // add: MenuItem null rejected, String null tolerated
        expectExact(NullPointerException.class, "add(MenuItem null)", () -> m.add((MenuItem) null));
        m.add((String) null);
        check(m.getItemCount() == 1 && m.getItem(0).getLabel() == null, "add(String null) accepted");
        m.add("a"); m.add("b");
        MenuItem c = new MenuItem("c");
        m.add(c);
        check(m.getItemCount() == 4 && m.getItem(3) == c, "add(MenuItem) identity and position");
        expectExact(ArrayIndexOutOfBoundsException.class, "getItem(99)", () -> m.getItem(99));
        // insert: negative rejected, beyond count appends
        expectExact(IllegalArgumentException.class, "insert(String,-1)", () -> m.insert("x", -1));
        m.insert("end", 99);
        check("end".equals(m.getItem(m.getItemCount() - 1).getLabel()), "insert beyond count appends");
        MenuItem ins = new MenuItem("ins");
        m.insert(ins, 1);
        check(m.getItem(1) == ins, "insert(MenuItem,int) position");
        // separators are plain "-" items
        m.addSeparator();
        check("-".equals(m.getItem(m.getItemCount() - 1).getLabel()), "addSeparator inserts '-' item");
        m.insertSeparator(0);
        check("-".equals(m.getItem(0).getLabel()), "insertSeparator(0)");
        expectExact(IllegalArgumentException.class, "insertSeparator(-1)", () -> m.insertSeparator(-1));
        // remove: bounds, by identity, removeAll
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(99)", () -> m.remove(99));
        int before = m.getItemCount();
        m.remove(c);
        check(m.getItemCount() == before - 1, "remove(MenuComponent) by identity");
        m.removeAll();
        check(m.getItemCount() == 0 && m.countItems() == 0, "removeAll and countItems alias");
        expectExact(ArrayIndexOutOfBoundsException.class, "remove(0) on empty menu", () -> m.remove(0));
        // a null MenuItem argument to insert() is rejected and, as a historic side
        // effect, clears the item list; add(null) rejects without that side effect
        Menu wipe = new Menu("w");
        wipe.add("a"); wipe.add("b");
        expectExact(NullPointerException.class, "insert(MenuItem null,0)",
              () -> wipe.insert((MenuItem) null, 0));
        check(wipe.getItemCount() == 0, "insert(MenuItem null) wipes the item list");
        expectExact(NullPointerException.class, "add(MenuItem null) on wipe",
              () -> wipe.add((MenuItem) null));
        check(wipe.getItemCount() == 0, "add(MenuItem null) leaves items untouched");

        // parent wiring through add/remove
        Menu parent = new Menu("P");
        MenuItem child = new MenuItem("ch");
        parent.add(child);
        check(child.getParent() == parent, "Menu.add wires child parent");
        parent.remove(child);
        // JDK 8 keeps the stale parent link after removal; JDK 9+ unwires it
        check(child.getParent() == null, "Menu.remove unwires child parent (JDK 9+)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMenu();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
