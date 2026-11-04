/*
 * @test
 * @key headful
 * @summary java.awt.Menu conformance: class reflection.
 * @run main MenuTest
 */

import java.awt.*;

public class MenuTest {
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
        check(child.getParent() == parent, "Menu.remove keeps the stale parent link (JDK 8)");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Menu");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuItem"), "superclass java.awt.MenuItem");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "isTearOff"), "method isTearOff");
        check(hasMethod(c, "getItemCount"), "method getItemCount");
        check(hasMethod(c, "countItems"), "method countItems");
        check(hasMethod(c, "getItem"), "method getItem");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "addSeparator"), "method addSeparator");
        check(hasMethod(c, "insertSeparator"), "method insertSeparator");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractMenu();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
