/*
 * @test
 * @summary javax.swing.plaf.SplitPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SplitPaneUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class SplitPaneUITest {
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
        runOnEDT(SplitPaneUITest::testWhiteBox);
        runOnEDT(SplitPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.SplitPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ComponentUI"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JSplitPane sp = new javax.swing.JSplitPane();
        javax.swing.plaf.SplitPaneUI ui = (javax.swing.plaf.SplitPaneUI) sp.getUI();
        check(ui != null, "splitpane UI non-null");
        ui.resetToPreferredSizes(sp);
        sp.setDividerLocation(100);
        check(ui.getDividerLocation(sp) >= 0, "divider location >= 0");
        check(ui.getMinimumDividerLocation(sp) >= 0, "min divider location");
        check(ui.getMaximumDividerLocation(sp) >= 0, "max divider location");
        ui.finishedPaintingChildren(sp, new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics());
    }
}
