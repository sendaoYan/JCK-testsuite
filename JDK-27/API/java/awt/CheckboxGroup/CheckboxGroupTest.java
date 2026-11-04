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
    static <T extends java.io.Serializable> T roundTrip(T obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            @SuppressWarnings("unchecked")
            T copy = (T) ois.readObject();
            return copy;
        }
    }

    static void testContractCheckboxGroup() throws Exception {
        CheckboxGroup g = new CheckboxGroup();
        check(g.getSelectedCheckbox() == null && g.getCurrent() == null, "no selection initially");
        Checkbox a = new Checkbox("a", false, g), b = new Checkbox("b", false, g);
        a.setState(true);
        check(g.getSelectedCheckbox() == a && g.getCurrent() == a,
              "getCurrent mirrors getSelectedCheckbox");
        // deprecated setCurrent behaves identically to setSelectedCheckbox
        g.setCurrent(b);
        check(g.getSelectedCheckbox() == b && !a.getState() && b.getState(), "setCurrent moves selection");
        g.setSelectedCheckbox(null);
        check(g.getSelectedCheckbox() == null && !b.getState(), "setSelectedCheckbox(null) unchecks member");
        // self-assignment keeps the selection
        g.setSelectedCheckbox(a);
        g.setSelectedCheckbox(a);
        check(g.getSelectedCheckbox() == a && a.getState(), "self-assignment keeps selection");
        // toString exposes the selected checkbox
        check(g.toString().startsWith("java.awt.CheckboxGroup[") && g.toString().contains("selectedCheckbox="),
              "toString carries selection field");
        g.setSelectedCheckbox(null);
        check(g.toString().contains("selectedCheckbox=null"), "toString shows null selection");
        // Serializable round trip
        check(java.io.Serializable.class.isAssignableFrom(CheckboxGroup.class), "implements Serializable");
        CheckboxGroup copy = roundTrip(g);
        check(copy != g && copy.getSelectedCheckbox() == null, "serialization round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractCheckboxGroup();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
