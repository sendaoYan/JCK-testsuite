/*
 * @test
 * @summary javax.swing.plaf.basic.BasicFileChooserUI.DoubleClickListener conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED DoubleClickListenerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class DoubleClickListenerTest {
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
        runOnEDT(DoubleClickListenerTest::testWhiteBox);
        runOnEDT(DoubleClickListenerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicFileChooserUI$DoubleClickListener");
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
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        javax.swing.plaf.basic.BasicFileChooserUI ui = new javax.swing.plaf.basic.BasicFileChooserUI(fc);
        ui.installUI(fc);
        javax.swing.JList<Object> list = new javax.swing.JList<>();
        java.awt.event.MouseListener h = (java.awt.event.MouseListener) reflectNew(
            "javax.swing.plaf.basic.BasicFileChooserUI$DoubleClickListener",
            new Class<?>[]{ javax.swing.plaf.basic.BasicFileChooserUI.class, javax.swing.JList.class }, ui, list);
        check(h != null, "obtained");
        h.mouseEntered(new java.awt.event.MouseEvent(list, java.awt.event.MouseEvent.MOUSE_ENTERED, 0L, 0, 0, 0, 0, false));
        h.mouseClicked(new java.awt.event.MouseEvent(list, java.awt.event.MouseEvent.MOUSE_CLICKED, 0L, 0, 0, 0, 1, false));
        check(true, "mouse events ok");
        ui.uninstallUI(fc);
    }
}
