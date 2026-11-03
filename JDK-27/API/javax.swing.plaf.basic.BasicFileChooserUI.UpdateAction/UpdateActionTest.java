/*
 * @test
 * @summary javax.swing.plaf.basic.BasicFileChooserUI.UpdateAction conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main UpdateActionTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class UpdateActionTest {
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
        runOnEDT(UpdateActionTest::testWhiteBox);
        runOnEDT(UpdateActionTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicFileChooserUI$UpdateAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractAction"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        javax.swing.plaf.basic.BasicFileChooserUI ui = new javax.swing.plaf.basic.BasicFileChooserUI(fc);
        ui.installUI(fc);
        javax.swing.Action a = ui.getUpdateAction();
        check(a != null, "update action non-null");
        check(a instanceof javax.swing.Action, "is Action");
        check(a.isEnabled() == true, "enabled by default");
        ui.uninstallUI(fc);
    }
}
