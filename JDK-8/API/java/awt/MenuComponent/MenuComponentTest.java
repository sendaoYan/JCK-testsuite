/*
 * @test
 * @key headful
 * @summary java.awt.MenuComponent conformance: abstract class reflection.
 * @run main MenuComponentTest
 */

import java.awt.*;

public class MenuComponentTest {
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
    static void testContractMenuComponent() throws Exception {
        MenuItem mi = new MenuItem("mc");
        check(mi.getParent() == null, "parent null before attach");
        Menu m = new Menu("m");
        m.add(mi);
        check(mi.getParent() == m, "Menu.add wires item parent");
        m.remove(mi);
        // JDK 8 keeps the stale parent link after removal; JDK 9+ unwires it
        check(mi.getParent() == m, "Menu.remove keeps the stale parent link (JDK 8)");
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
        Class<?> c = Class.forName("java.awt.MenuComponent");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "setName"), "method setName");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getPeer"), "method getPeer");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "postEvent"), "method postEvent");
        check(hasMethod(c, "dispatchEvent"), "method dispatchEvent");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractMenuComponent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
