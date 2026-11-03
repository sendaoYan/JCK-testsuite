/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboPopup.ItemHandler conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED ItemHandlerTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class ItemHandlerTest {
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
        runOnEDT(ItemHandlerTest::testWhiteBox);
        runOnEDT(ItemHandlerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboPopup$ItemHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.ItemListener"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<>();
        javax.swing.plaf.basic.BasicComboPopup popup = new javax.swing.plaf.basic.BasicComboPopup(cb);
        java.awt.event.ItemListener h = (java.awt.event.ItemListener) reflectNew(
            "javax.swing.plaf.basic.BasicComboPopup$ItemHandler",
            new Class<?>[]{ javax.swing.plaf.basic.BasicComboPopup.class }, popup);
        check(h != null, "obtained");
        h.itemStateChanged(new java.awt.event.ItemEvent(cb, java.awt.event.ItemEvent.ITEM_STATE_CHANGED, cb, java.awt.event.ItemEvent.SELECTED));
        check(true, "itemStateChanged ok");
    }
}
