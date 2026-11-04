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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.Insets serInsets(java.awt.Insets i) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(i);
        return (java.awt.Insets) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        java.awt.Insets i = new java.awt.Insets(1, 2, 3, 4);
        // field order is (top, left, bottom, right) - classic source of bugs
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "ctor field order top,left,bottom,right");
        // set()
        i.set(9, 8, 7, 6);
        check(i.top == 9 && i.left == 8 && i.bottom == 7 && i.right == 6, "set() same order");
        // negative insets permitted
        java.awt.Insets neg = new java.awt.Insets(-1, -2, -3, -4);
        check(neg.top == -1, "negative insets allowed");
        // equals/hashCode contract
        check(new java.awt.Insets(1, 2, 3, 4).equals(new java.awt.Insets(1, 2, 3, 4)), "equals");
        check(!new java.awt.Insets(1, 2, 3, 4).equals(new java.awt.Insets(4, 3, 2, 1)), "reversed not equal");
        check(!new java.awt.Insets(1, 2, 3, 4).equals(new java.awt.Insets(1, 2, 3, 5)), "one field differs");
        check(new java.awt.Insets(1, 2, 3, 4).hashCode() == new java.awt.Insets(1, 2, 3, 4).hashCode(),
              "hashCode equal for equal");
        check(new java.awt.Insets(1, 2, 3, 4).hashCode() == 577, "hashCode exact for (1,2,3,4): " + new java.awt.Insets(1,2,3,4).hashCode());
        check(!new java.awt.Insets(1, 2, 3, 4).equals(null), "equals(null)");
        check(!new java.awt.Insets(1, 2, 3, 4).equals("insets"), "equals unrelated");
        // toString exact format
        check(new java.awt.Insets(1, 2, 3, 4).toString().equals("java.awt.Insets[top=1,left=2,bottom=3,right=4]"),
              "toString exact");
        // clone (Cloneable)
        java.awt.Insets cl = (java.awt.Insets) i.clone();
        check(cl.equals(i), "clone equal");
        cl.top = 100;
        check(i.top == 9, "clone independent");
        // serialization round-trip incl. negative
        java.awt.Insets rt = serInsets(neg);
        check(rt.top == -1 && rt.left == -2 && rt.bottom == -3 && rt.right == -4, "serialization round-trip");
        check(java.io.ObjectStreamClass.lookup(java.awt.Insets.class).getSerialVersionUID()
              == -2272572637695466749L, "serialVersionUID");
        check(rt.toString().equals("java.awt.Insets[top=-1,left=-2,bottom=-3,right=-4]"), "toString after round-trip");
        // set mutates all four fields atomically
        java.awt.Insets mut = new java.awt.Insets(0, 0, 0, 0);
        mut.set(1, 2, 3, 4); mut.set(5, 6, 7, 8);
        check(mut.top == 5 && mut.left == 6 && mut.bottom == 7 && mut.right == 8, "set overwrites previous");
    }

}
