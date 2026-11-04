/*
 * @test
 * @summary java.awt.CheckboxGroup conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CheckboxGroupTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class CheckboxGroupTest {
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
        Class<?> c = Class.forName("java.awt.CheckboxGroup");
        check(c != null, "type loads: java.awt.CheckboxGroup");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean serializable = false;
        for (Class<?> i : c.getInterfaces()) if (i == java.io.Serializable.class) serializable = true;
        check(serializable, "implements Serializable");
        for (String name : new String[]{"getSelectedCheckbox", "getCurrent",
                "setSelectedCheckbox", "setCurrent", "toString"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CheckboxGroup g = new CheckboxGroup();
        check(g.getSelectedCheckbox() == null, "selected null initially");
        check(g.getCurrent() == null, "current null initially");

        Checkbox a = new Checkbox("a", false, g);
        Checkbox b = new Checkbox("b", true, g);
        check(g.getSelectedCheckbox() == b, "constructor selects true checkbox");
        check(g.getCurrent() == b, "getCurrent returns selection");

        g.setSelectedCheckbox(a);
        check(g.getSelectedCheckbox() == a, "setSelectedCheckbox");
        check(a.getState() && !b.getState(), "setSelectedCheckbox updates states");
        g.setCurrent(b);
        check(g.getCurrent() == b, "setCurrent");
        check(g.getSelectedCheckbox() == b, "setCurrent updates selected");
        check(b.getState() && !a.getState(), "setCurrent updates states");

        check(g.toString() != null && g.toString().contains("selectedCheckbox"), "toString format");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
