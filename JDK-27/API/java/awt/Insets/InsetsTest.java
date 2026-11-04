/*
 * @test
 * @summary java.awt.Insets conformance: white-box structure + black-box behavior.
 * @run main InsetsTest
 */

import java.lang.reflect.*;

public class InsetsTest {
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
        Class<?> c = Class.forName("java.awt.Insets");
        check(c != null, "type loads: java.awt.Insets");
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
        check(hasMethod(c, "set"), "has set()");
        check(hasMethod(c, "equals"), "has equals()");
        check(hasMethod(c, "hashCode"), "has hashCode()");
        check(hasMethod(c, "clone"), "has clone()");
        check(hasMethod(c, "toString"), "has toString()");
        for (String f : new String[]{"top", "left", "bottom", "right"}) {
            check(hasField(c, f), "public field " + f);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.Insets in = new java.awt.Insets(1, 2, 3, 4);
        check(in.top == 1 && in.left == 2 && in.bottom == 3 && in.right == 4,
              "constructor stores fields");
        in.set(5, 6, 7, 8);
        check(in.top == 5 && in.left == 6 && in.bottom == 7 && in.right == 8,
              "set() updates all four fields");

        java.awt.Insets a = new java.awt.Insets(1, 2, 3, 4);
        java.awt.Insets b = new java.awt.Insets(1, 2, 3, 4);
        java.awt.Insets c = new java.awt.Insets(1, 2, 3, 9);
        check(a.equals(b), "equals same values");
        check(!a.equals(c), "not equals different value");
        check(!a.equals(null), "not equals null");
        check(!a.equals("not insets"), "not equals other type");
        check(a.hashCode() == b.hashCode(), "equal objects have equal hashCode");

        java.awt.Insets d = (java.awt.Insets) a.clone();
        check(d != a, "clone is a distinct object");
        check(d.equals(a), "clone equals original");
        d.top = 99;
        check(a.top == 1, "mutating clone does not affect original");

        check(a.toString().equals("java.awt.Insets[top=1,left=2,bottom=3,right=4]"),
              "toString format");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
