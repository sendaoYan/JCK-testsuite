/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboPopup conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicComboPopupTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicComboPopupTest {
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
        runOnEDT(BasicComboPopupTest::testWhiteBox);
        runOnEDT(BasicComboPopupTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboPopup");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JPopupMenu"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.basic.ComboPopup"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<>(new Object[]{"a", "b"});
        javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(cb);
        check(popup.getList() != null, "list non-null");
        check(popup.getList().getModel().getSize() == 2, "list model size 2");
        check(popup.getMouseListener() != null, "mouse listener");
        check(popup.getMouseMotionListener() != null, "mouse motion listener");
        popup.getKeyListener();
        check(true, "getKeyListener callable");
        check(popup.isFocusTraversable() == false, "not focus traversable");
        popup.uninstallingUI();
        check(true, "uninstallingUI ok");
    }
}
