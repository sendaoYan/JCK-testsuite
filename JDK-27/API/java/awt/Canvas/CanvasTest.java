/*
 * @test
 * @summary java.awt.Canvas conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CanvasTest
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class CanvasTest {
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
        Class<?> c = Class.forName("java.awt.Canvas");
        check(c != null, "type loads: java.awt.Canvas");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        for (String name : new String[]{"paint", "update", "createBufferStrategy", "getBufferStrategy"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Canvas c = new Canvas();
        check(c.getBufferStrategy() == null, "buffer strategy null before peer");

        // paint/update should run against an in-memory graphics without throwing
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        c.setSize(50, 50);
        c.paint(g);
        c.update(g);
        g.dispose();

        Frame f = new Frame("CanvasTest");
        f.setLayout(null);
        f.setSize(200, 200);
        f.setVisible(true);
        try {
            f.add(c);
            check(c.isDisplayable(), "displayable after add");
            check(c.isShowing(), "showing after add");
            c.setBounds(0, 0, 100, 80);
            check(c.getBounds().equals(new Rectangle(0, 0, 100, 80)), "setBounds/getBounds");
            c.setBackground(Color.CYAN);
            check(Color.CYAN.equals(c.getBackground()), "setBackground/getBackground");
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
        testWhiteBox();
        testBlackBox();
        testContractCanvas();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
