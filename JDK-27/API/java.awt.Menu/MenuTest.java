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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
