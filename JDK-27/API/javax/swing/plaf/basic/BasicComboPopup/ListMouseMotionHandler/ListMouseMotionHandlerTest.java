/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboPopup.ListMouseMotionHandler conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED ListMouseMotionHandlerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class ListMouseMotionHandlerTest {
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
        runOnEDT(ListMouseMotionHandlerTest::testWhiteBox);
        runOnEDT(ListMouseMotionHandlerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboPopup$ListMouseMotionHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseMotionAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<>();
        javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(cb);
        java.awt.event.MouseMotionListener h = (java.awt.event.MouseMotionListener) reflectNew(
            "javax.swing.plaf.basic.BasicComboPopup$ListMouseMotionHandler",
            new Class<?>[]{ javax.swing.plaf.basic.BasicComboPopup.class }, popup);
        check(h != null, "obtained");
        h.mouseMoved(new java.awt.event.MouseEvent(cb, java.awt.event.MouseEvent.MOUSE_MOVED, 0L, 0, 0, 0, 0, false));
        check(true, "mouseMoved ok");
    }
}
