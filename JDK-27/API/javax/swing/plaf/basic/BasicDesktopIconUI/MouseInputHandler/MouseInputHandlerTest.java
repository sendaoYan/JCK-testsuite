/*
 * @test
 * @summary javax.swing.plaf.basic.BasicDesktopIconUI.MouseInputHandler conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MouseInputHandlerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class MouseInputHandlerTest {
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
        runOnEDT(MouseInputHandlerTest::testWhiteBox);
        runOnEDT(MouseInputHandlerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicDesktopIconUI$MouseInputHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.event.MouseInputAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JInternalFrame f = new javax.swing.JInternalFrame();
        javax.swing.JInternalFrame.JDesktopIcon icon = f.getDesktopIcon();
        javax.swing.plaf.basic.BasicDesktopIconUI ui = new javax.swing.plaf.basic.BasicDesktopIconUI();
        ui.installUI(icon);
        javax.swing.plaf.basic.BasicDesktopIconUI.MouseInputHandler h = ui.new MouseInputHandler();
        check(h instanceof javax.swing.event.MouseInputListener, "is MouseInputListener");
        h.mouseReleased(new java.awt.event.MouseEvent(icon, java.awt.event.MouseEvent.MOUSE_RELEASED, 0L, 0, 0, 0, 1, false));
        h.mouseMoved(new java.awt.event.MouseEvent(icon, java.awt.event.MouseEvent.MOUSE_MOVED, 0L, 0, 0, 0, 0, false));
        check(true, "mouse events ok");
        ui.uninstallUI(icon);
    }
}
