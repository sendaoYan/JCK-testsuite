/*
 * @test
 * @summary java.awt.Label conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main LabelTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class LabelTest {
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
        Class<?> c = Class.forName("java.awt.Label");
        check(c != null, "type loads: java.awt.Label");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        for (String name : new String[]{"getText", "setText", "getAlignment", "setAlignment"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(Label.LEFT == 0, "LEFT 0");
        check(Label.CENTER == 1, "CENTER 1");
        check(Label.RIGHT == 2, "RIGHT 2");

        Label l = new Label();
        check("".equals(l.getText()), "default empty text");
        check(l.getAlignment() == Label.LEFT, "default LEFT");

        Label l2 = new Label("hi");
        check("hi".equals(l2.getText()), "constructor text");
        check(l2.getAlignment() == Label.LEFT, "constructor LEFT");

        Label l3 = new Label("hi", Label.RIGHT);
        check(l3.getAlignment() == Label.RIGHT, "constructor alignment");

        l2.setText("changed");
        check("changed".equals(l2.getText()), "setText/getText");
        l2.setAlignment(Label.CENTER);
        check(l2.getAlignment() == Label.CENTER, "setAlignment/getAlignment");

        Frame f = new Frame("LabelTest");
        f.setSize(100, 100);
        f.setVisible(true);
        try {
            f.add(l2);
            check(l2.isDisplayable(), "displayable after add");
            check(l2.isShowing(), "showing after add");
        } finally {
            f.dispose();
        }
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
        testWhiteBox();
        testBlackBox();
        testContractLabel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
