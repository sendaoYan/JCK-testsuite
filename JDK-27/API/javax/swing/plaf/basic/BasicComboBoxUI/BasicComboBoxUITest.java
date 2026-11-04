/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboBoxUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicComboBoxUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicComboBoxUITest {
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
        runOnEDT(BasicComboBoxUITest::testWhiteBox);
        runOnEDT(BasicComboBoxUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboBoxUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ComboBoxUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<>(new Object[]{"a", "b", "c"});
        javax.swing.plaf.basic.BasicComboBoxUI ui = new javax.swing.plaf.basic.BasicComboBoxUI();
        check(javax.swing.plaf.basic.BasicComboBoxUI.createUI(cb) instanceof javax.swing.plaf.basic.BasicComboBoxUI, "createUI");
        cb.setUI(ui);
        check(cb.getUI() == ui, "installed");
        check(ui.getPreferredSize(cb) != null, "preferred");
        check(ui.isPopupVisible(cb) == false, "popup hidden");
        cb.setUI(null);
    }
}
