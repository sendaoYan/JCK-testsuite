/*
 * @test
 * @summary java.awt.GridBagConstraints conformance: white-box structure + black-box behavior.
 * @run main GridBagConstraintsTest
 */

import java.lang.reflect.*;

public class GridBagConstraintsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static boolean hasField(Class<?> c, String name) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GridBagConstraints");
        check(c != null, "type loads: java.awt.GridBagConstraints");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean cloneable = false, serializable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == Cloneable.class) cloneable = true;
            if (i == java.io.Serializable.class) serializable = true;
        }
        check(cloneable, "implements Cloneable");
        check(serializable, "implements Serializable");
        check(hasMethod(c, "clone"), "has clone()");
        for (String f : new String[]{"gridx", "gridy", "gridwidth", "gridheight",
                "weightx", "weighty", "anchor", "fill", "insets", "ipadx", "ipady"}) {
            check(hasField(c, f), "public field " + f);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        check(c.gridx == java.awt.GridBagConstraints.RELATIVE, "default gridx RELATIVE");
        check(c.gridy == java.awt.GridBagConstraints.RELATIVE, "default gridy RELATIVE");
        check(c.gridwidth == 1, "default gridwidth 1");
        check(c.gridheight == 1, "default gridheight 1");
        check(c.weightx == 0.0 && c.weighty == 0.0, "default weight 0");
        check(c.anchor == java.awt.GridBagConstraints.CENTER, "default anchor CENTER");
        check(c.fill == java.awt.GridBagConstraints.NONE, "default fill NONE");
        check(c.insets.top == 0 && c.insets.left == 0 && c.insets.bottom == 0 && c.insets.right == 0,
              "default insets zero");
        check(c.ipadx == 0 && c.ipady == 0, "default ipad zero");

        // constants
        check(java.awt.GridBagConstraints.RELATIVE == -1, "RELATIVE -1");
        check(java.awt.GridBagConstraints.REMAINDER == 0, "REMAINDER 0");
        check(java.awt.GridBagConstraints.NONE == 0, "NONE 0");
        check(java.awt.GridBagConstraints.BOTH == 1, "BOTH 1");
        check(java.awt.GridBagConstraints.HORIZONTAL == 2, "HORIZONTAL 2");
        check(java.awt.GridBagConstraints.VERTICAL == 3, "VERTICAL 3");
        check(java.awt.GridBagConstraints.CENTER == 10, "CENTER 10");
        check(java.awt.GridBagConstraints.NORTH == 11, "NORTH 11");
        check(java.awt.GridBagConstraints.EAST == 13, "EAST 13");
        check(java.awt.GridBagConstraints.SOUTH == 15, "SOUTH 15");
        check(java.awt.GridBagConstraints.WEST == 17, "WEST 17");
        check(java.awt.GridBagConstraints.BASELINE == 0x100, "BASELINE 256");
        check(java.awt.GridBagConstraints.ABOVE_BASELINE == 0x400, "ABOVE_BASELINE 1024");
        check(java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING == java.awt.GridBagConstraints.ABOVE_BASELINE + 0x100,
              "ABOVE_BASELINE_LEADING == ABOVE_BASELINE + 256");

        // 11-arg constructor
        java.awt.Insets ins = new java.awt.Insets(1, 2, 3, 4);
        java.awt.GridBagConstraints c2 = new java.awt.GridBagConstraints(
                1, 2, 3, 4, 0.5, 0.25, java.awt.GridBagConstraints.NORTHWEST,
                java.awt.GridBagConstraints.HORIZONTAL, ins, 7, 8);
        check(c2.gridx == 1 && c2.gridy == 2 && c2.gridwidth == 3 && c2.gridheight == 4,
              "11-arg grid fields");
        check(c2.weightx == 0.5 && c2.weighty == 0.25, "11-arg weights");
        check(c2.anchor == java.awt.GridBagConstraints.NORTHWEST
                && c2.fill == java.awt.GridBagConstraints.HORIZONTAL, "11-arg anchor/fill");
        check(c2.insets == ins, "11-arg insets identity");
        check(c2.ipadx == 7 && c2.ipady == 8, "11-arg ipad");

        // clone independence (insets deep-copied)
        java.awt.GridBagConstraints c3 = (java.awt.GridBagConstraints) c2.clone();
        check(c3 != c2, "clone distinct object");
        check(c3.gridx == c2.gridx && c3.insets.equals(c2.insets), "clone equal values");
        check(c3.insets != c2.insets, "clone insets deep copy");
        c3.insets.top = 99;
        check(c2.insets.top == 1, "mutating clone insets does not affect original");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
