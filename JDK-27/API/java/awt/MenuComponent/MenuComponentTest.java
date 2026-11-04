/*
 * @test
 * @summary java.awt.MenuComponent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MenuComponentTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class MenuComponentTest {
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
        Class<?> c = Class.forName("java.awt.MenuComponent");
        check(c != null, "type loads: java.awt.MenuComponent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean serializable = false;
        for (Class<?> i : c.getInterfaces()) if (i == java.io.Serializable.class) serializable = true;
        check(serializable, "implements Serializable");
        for (String name : new String[]{"getName", "setName", "getParent", "getFont", "setFont",
                "removeNotify", "postEvent", "dispatchEvent", "toString"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // MenuComponent is abstract; exercise inherited behavior via MenuItem.
        MenuItem mi = new MenuItem("x");
        check(mi instanceof MenuComponent, "MenuItem is a MenuComponent");
        check(mi.getParent() == null, "no parent initially");
        check(mi.getName() != null, "default name non-null");
        mi.setName("comp");
        check("comp".equals(mi.getName()), "setName/getName");

        Font f = new Font("Dialog", Font.BOLD, 14);
        mi.setFont(f);
        check(f.equals(mi.getFont()), "setFont/getFont");

        check(mi.toString() != null && mi.toString().contains("comp"), "toString contains name");
        mi.removeNotify(); // no-op before peer
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
    static void testContractMenuComponent() throws Exception {
        MenuItem mi = new MenuItem("mc");
        check(mi.getParent() == null, "parent null before attach");
        Menu m = new Menu("m");
        m.add(mi);
        check(mi.getParent() == m, "Menu.add wires item parent");
        m.remove(mi);
        // JDK 8 keeps the stale parent link after removal; JDK 9+ unwires it
        check(mi.getParent() == null, "Menu.remove unwires item parent (JDK 9+)");
        check(mi.getName() != null, "generated name non-null");
        mi.setName("named");
        check("named".equals(mi.getName()), "setName/getName roundtrip");
        Font f = new Font("Dialog", Font.PLAIN, 7);
        mi.setFont(f);
        check(f.equals(mi.getFont()), "setFont/getFont roundtrip");
        check(mi.getFont() != null, "explicit font retrievable");
        mi.setFont(null);
        check(mi.getFont() == null, "setFont(null) clears the font (no fallback before a peer)");
        check(mi.toString() != null, "toString non-null");
        //@8 check(mi.getPeer() == null, "deprecated getPeer() is null without a peer");
        //@27 boolean peerGone = false;
        //@27 try { Class.forName("java.awt.MenuComponent").getMethod("getPeer"); }
        //@27 catch (NoSuchMethodException expected) { peerGone = true; }
        //@27 check(peerGone, "deprecated getPeer removed (JDK 9+)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMenuComponent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
