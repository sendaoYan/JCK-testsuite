/*
 * @test
 * @key headful
 * @summary java.awt.Label conformance: class reflection.
 * @run main LabelTest
 */

import java.awt.*;

public class LabelTest {
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
    static void testContractLabel() throws Exception {
        check(Label.LEFT == 0 && Label.CENTER == 1 && Label.RIGHT == 2, "alignment constant values");
        Label l = new Label();
        check("".equals(l.getText()) && l.getAlignment() == Label.LEFT, "Label() defaults");
        check(new Label(null).getText() == null, "Label(null) retains null");
        check(new Label(null, Label.CENTER).getAlignment() == Label.CENTER, "Label(null,alignment)");
        l.setText(null);
        check(l.getText() == null, "setText(null) retains null");
        l.setText("t");
        check("t".equals(l.getText()), "setText/getText roundtrip");
        l.setAlignment(Label.RIGHT);
        check(l.getAlignment() == Label.RIGHT, "setAlignment/getAlignment roundtrip");
        expectExact(IllegalArgumentException.class, "setAlignment(99)", () -> l.setAlignment(99));
        expectExact(IllegalArgumentException.class, "setAlignment(-1)", () -> l.setAlignment(-1));
        check(l.getAlignment() == Label.RIGHT, "failed setAlignment keeps value");
        expectExact(IllegalArgumentException.class, "new Label(t,99)", () -> new Label("t", 99));
        expectExact(IllegalArgumentException.class, "new Label(t,-1)", () -> new Label("t", -1));
        check(new Label("t", Label.RIGHT).getAlignment() == Label.RIGHT
              && new Label("t", Label.CENTER).getAlignment() == Label.CENTER,
              "valid ctor alignments accepted");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Label");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getAlignment"), "method getAlignment");
        check(hasMethod(c, "setAlignment"), "method setAlignment");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractLabel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
