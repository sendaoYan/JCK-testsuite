/*
 * @test
 * @key headful
 * @summary java.awt.Canvas conformance: class reflection.
 * @run main CanvasTest
 */

import java.awt.*;

public class CanvasTest {
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

    static void testContractCanvas() throws Exception {
        Canvas fresh = new Canvas();
        check(fresh.getBufferStrategy() == null, "no buffer strategy before peer");
        // null Graphics must fail fast with NPE in paint/update
        expectExact(NullPointerException.class, "paint(null)", () -> new Canvas().paint(null));
        expectExact(NullPointerException.class, "update(null)", () -> new Canvas().update(null));
        // accessible context type
        check("java.awt.Canvas$AccessibleAWTCanvas".equals(fresh.getAccessibleContext().getClass().getName()),
              "AccessibleAWTCanvas context type");
        // Component-level data property roundtrips
        fresh.setName("cv");
        check("cv".equals(fresh.getName()), "setName/getName roundtrip");
        Color bg = new Color(10, 20, 30);
        fresh.setBackground(bg);
        check(bg.equals(fresh.getBackground()), "setBackground/getBackground roundtrip");
        fresh.setForeground(Color.WHITE);
        check(Color.WHITE.equals(fresh.getForeground()), "setForeground/getForeground roundtrip");
        fresh.setEnabled(false);
        check(!fresh.isEnabled(), "setEnabled(false)");
        // serialization round trip keeps the data properties
        Canvas copy = roundTrip(fresh);
        check(copy != fresh, "serialized copy is a distinct object");
        check("cv".equals(copy.getName()), "serialization keeps name");
        check(bg.equals(copy.getBackground()), "serialization keeps background");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Canvas");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "paint"), "method paint");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "createBufferStrategy"), "method createBufferStrategy");
        check(hasMethod(c, "getBufferStrategy"), "method getBufferStrategy");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractCanvas();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
