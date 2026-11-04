/*
 * @test
 * @summary javax.swing.plaf.basic.BasicArrowButton conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicArrowButtonTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicArrowButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    static Object reflectNew(String className, Class<?>[] paramTypes, Object... args) throws Exception {
        Class<?> c = Class.forName(className);
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(paramTypes);
        ctor.setAccessible(true);
        return ctor.newInstance(args);
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(BasicArrowButtonTest::testWhiteBox);
        runOnEDT(BasicArrowButtonTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicArrowButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicArrowButton b = new javax.swing.plaf.basic.BasicArrowButton(javax.swing.SwingConstants.NORTH);
        check(b.getDirection() == javax.swing.SwingConstants.NORTH, "direction ctor");
        b.setDirection(javax.swing.SwingConstants.SOUTH);
        check(b.getDirection() == javax.swing.SwingConstants.SOUTH, "setDirection");
        check(b.getPreferredSize() != null, "preferred size");
        check(b.getMinimumSize() != null, "minimum size");
        check(b.getMaximumSize() != null, "maximum size");
        check(b.isFocusTraversable() == false, "not focus traversable");
        java.awt.Graphics g = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        b.paint(g);
        g.dispose();
        check(true, "paint ok");
        javax.swing.plaf.basic.BasicArrowButton b2 = new javax.swing.plaf.basic.BasicArrowButton(javax.swing.SwingConstants.EAST, java.awt.Color.GRAY, java.awt.Color.DARK_GRAY, java.awt.Color.WHITE, java.awt.Color.LIGHT_GRAY);
        check(b2.getDirection() == javax.swing.SwingConstants.EAST, "5-arg ctor direction");
    }
}
