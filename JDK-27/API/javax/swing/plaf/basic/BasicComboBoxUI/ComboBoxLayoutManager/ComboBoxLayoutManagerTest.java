/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboBoxUI.ComboBoxLayoutManager conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ComboBoxLayoutManagerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class ComboBoxLayoutManagerTest {
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
        runOnEDT(ComboBoxLayoutManagerTest::testWhiteBox);
        runOnEDT(ComboBoxLayoutManagerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboBoxUI$ComboBoxLayoutManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicComboBoxUI ui = new javax.swing.plaf.basic.BasicComboBoxUI();
        javax.swing.plaf.basic.BasicComboBoxUI.ComboBoxLayoutManager lm = ui.new ComboBoxLayoutManager();
        check(lm instanceof java.awt.LayoutManager, "is LayoutManager");
        lm.addLayoutComponent("x", new javax.swing.JLabel());
        lm.removeLayoutComponent(new javax.swing.JLabel());
        check(true, "no-op layout methods");
    }
}
