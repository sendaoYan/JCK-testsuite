/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboBoxUI.KeyHandler conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main KeyHandlerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class KeyHandlerTest {
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
        runOnEDT(KeyHandlerTest::testWhiteBox);
        runOnEDT(KeyHandlerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboBoxUI$KeyHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.KeyAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<>();
        javax.swing.plaf.basic.BasicComboBoxUI ui = new javax.swing.plaf.basic.BasicComboBoxUI();
        ui.installUI(cb);
        javax.swing.plaf.basic.BasicComboBoxUI.KeyHandler h = ui.new KeyHandler();
        h.keyPressed(new java.awt.event.KeyEvent(cb, java.awt.event.KeyEvent.KEY_PRESSED, 0L, 0, java.awt.event.KeyEvent.VK_DOWN, ' '));
        check(true, "keyPressed ok");
        ui.uninstallUI(cb);
    }
}
