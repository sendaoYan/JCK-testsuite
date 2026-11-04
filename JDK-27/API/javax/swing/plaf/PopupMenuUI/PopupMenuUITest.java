/*
 * @test
 * @summary javax.swing.plaf.PopupMenuUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main PopupMenuUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class PopupMenuUITest {
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
        runOnEDT(PopupMenuUITest::testWhiteBox);
        runOnEDT(PopupMenuUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.PopupMenuUI");
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
        javax.swing.JPopupMenu pm = new javax.swing.JPopupMenu();
        javax.swing.plaf.PopupMenuUI ui = (javax.swing.plaf.PopupMenuUI) pm.getUI();
        check(ui != null, "popupmenu UI non-null");
        check(ui.isPopupTrigger(new java.awt.event.MouseEvent(pm, java.awt.event.MouseEvent.MOUSE_PRESSED, 0L, 0, 0, 0, 1, false)) == false, "popup trigger false");
        javax.swing.Popup pop = ui.getPopup(pm, 0, 0);
        check(pop != null, "getPopup non-null");
    }
}
