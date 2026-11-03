/*
 * @test
 * @summary javax.swing.plaf.basic.BasicInternalFrameUI.BorderListener conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED BorderListenerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BorderListenerTest {
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
        runOnEDT(BorderListenerTest::testWhiteBox);
        runOnEDT(BorderListenerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicInternalFrameUI$BorderListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.event.MouseInputAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JInternalFrame f = new javax.swing.JInternalFrame("t");
        javax.swing.plaf.basic.BasicInternalFrameUI ui = new javax.swing.plaf.basic.BasicInternalFrameUI(f);
        ui.installUI(f);
        java.awt.event.MouseListener l = (java.awt.event.MouseListener) reflectNew(
            "javax.swing.plaf.basic.BasicInternalFrameUI$BorderListener",
            new Class<?>[]{ javax.swing.plaf.basic.BasicInternalFrameUI.class }, ui);
        check(l != null, "obtained");
        l.mouseEntered(new java.awt.event.MouseEvent(f, java.awt.event.MouseEvent.MOUSE_ENTERED, 0L, 0, 0, 0, 0, false));
        l.mouseExited(new java.awt.event.MouseEvent(f, java.awt.event.MouseEvent.MOUSE_EXITED, 0L, 0, 0, 0, 0, false));
        check(true, "mouse events ok");
        ui.uninstallUI(f);
    }
}
