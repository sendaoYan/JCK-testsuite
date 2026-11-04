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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
