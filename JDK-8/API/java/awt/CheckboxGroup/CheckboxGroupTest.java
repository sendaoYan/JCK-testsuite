/*
 * @test
 * @key headful
 * @summary java.awt.CheckboxGroup conformance: class reflection.
 * @run main CheckboxGroupTest
 */

import java.awt.*;

public class CheckboxGroupTest {
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
        Class<?> c = Class.forName("java.awt.CheckboxGroup");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getSelectedCheckbox"), "method getSelectedCheckbox");
        check(hasMethod(c, "getCurrent"), "method getCurrent");
        check(hasMethod(c, "setSelectedCheckbox"), "method setSelectedCheckbox");
        check(hasMethod(c, "setCurrent"), "method setCurrent");
        check(hasMethod(c, "toString"), "method toString");
        testContractCheckboxGroup();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
