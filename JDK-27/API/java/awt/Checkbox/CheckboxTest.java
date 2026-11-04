/*
 * @test
 * @summary java.awt.Checkbox conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CheckboxTest
 */

import java.awt.*;
import java.awt.event.ItemListener;
import java.lang.reflect.*;

public class CheckboxTest {
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
        Class<?> c = Class.forName("java.awt.Checkbox");
        check(c != null, "type loads: java.awt.Checkbox");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean itemSelectable = false, accessible = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == ItemSelectable.class) itemSelectable = true;
            if (i == javax.accessibility.Accessible.class) accessible = true;
        }
        check(itemSelectable, "implements ItemSelectable");
        check(accessible, "implements Accessible");
        for (String name : new String[]{"getLabel", "setLabel", "getState", "setState",
                "getSelectedObjects", "getCheckboxGroup", "setCheckboxGroup",
                "addItemListener", "removeItemListener", "getItemListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Checkbox cb = new Checkbox();
        check("".equals(cb.getLabel()), "default empty label");
        check(!cb.getState(), "default state false");
        check(cb.getCheckboxGroup() == null, "default no group");

        Checkbox cb2 = new Checkbox("opt", true);
        check("opt".equals(cb2.getLabel()), "constructor label");
        check(cb2.getState(), "constructor state true");

        cb2.setLabel("opt2");
        check("opt2".equals(cb2.getLabel()), "setLabel/getLabel");
        cb2.setState(false);
        check(!cb2.getState(), "setState(false)");
        cb2.setState(true);
        check(cb2.getState(), "setState(true)");

        check(cb2.getSelectedObjects() != null && cb2.getSelectedObjects().length == 1
                && "opt2".equals(cb2.getSelectedObjects()[0]), "getSelectedObjects when selected");
        cb2.setState(false);
        check(cb2.getSelectedObjects() == null, "getSelectedObjects null when not selected");

        ItemListener l = e -> {};
        cb2.addItemListener(l);
        check(cb2.getItemListeners().length == 1 && cb2.getItemListeners()[0] == l,
              "addItemListener/getItemListeners");
        cb2.removeItemListener(l);
        check(cb2.getItemListeners().length == 0, "removeItemListener");

        // CheckboxGroup interaction
        CheckboxGroup g = new CheckboxGroup();
        Checkbox a = new Checkbox("a", false, g);
        Checkbox b = new Checkbox("b", true, g);
        check(a.getCheckboxGroup() == g && b.getCheckboxGroup() == g, "group assigned");
        check(g.getSelectedCheckbox() == b, "group selected checkbox is the true one");
        b.setCheckboxGroup(null);
        check(b.getCheckboxGroup() == null, "setCheckboxGroup(null)");

        Frame f = new Frame("CheckboxTest");
        f.setSize(150, 150);
        f.setVisible(true);
        try {
            f.add(cb2);
            check(cb2.isDisplayable(), "displayable after add");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
