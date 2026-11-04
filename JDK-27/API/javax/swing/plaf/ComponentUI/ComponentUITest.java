/*
 * @test
 * @summary javax.swing.plaf.ComponentUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ComponentUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class ComponentUITest {
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
        runOnEDT(ComponentUITest::testWhiteBox);
        runOnEDT(ComponentUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.ComponentUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JButton b = new javax.swing.JButton();
        javax.swing.plaf.ComponentUI ui = b.getUI();
        check(ui != null, "component UI non-null");
        boolean threw = false;
        try { javax.swing.plaf.ComponentUI.createUI(b); }
        catch (java.lang.Error ex) { threw = true; }
        check(threw, "base createUI throws Error");
        ui.installUI(b);
        ui.uninstallUI(b);
        check(ui.getPreferredSize(b) != null, "preferred size non-null");
        check(ui.getMinimumSize(b) != null, "minimum size non-null");
        check(ui.getMaximumSize(b) != null, "maximum size non-null");
        check(ui.getAccessibleChildrenCount(b) == 0, "accessible children 0");
    }
}
