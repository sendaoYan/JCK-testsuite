/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboPopup.InvocationMouseHandler conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED InvocationMouseHandlerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class InvocationMouseHandlerTest {
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
        runOnEDT(InvocationMouseHandlerTest::testWhiteBox);
        runOnEDT(InvocationMouseHandlerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboPopup$InvocationMouseHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<>();
        javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(cb);
        java.awt.event.MouseListener h = (java.awt.event.MouseListener) reflectNew(
            "javax.swing.plaf.basic.BasicComboPopup$InvocationMouseHandler",
            new Class<?>[]{ javax.swing.plaf.basic.BasicComboPopup.class }, popup);
        check(h != null, "obtained");
        h.mousePressed(new java.awt.event.MouseEvent(cb, java.awt.event.MouseEvent.MOUSE_PRESSED, 0L, 0, 0, 0, 1, false));
        h.mouseReleased(new java.awt.event.MouseEvent(cb, java.awt.event.MouseEvent.MOUSE_RELEASED, 0L, 0, 0, 0, 1, false));
        check(true, "mouse events ok");
    }
}
