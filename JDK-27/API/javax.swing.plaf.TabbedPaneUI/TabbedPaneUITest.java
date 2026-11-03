/*
 * @test
 * @summary javax.swing.plaf.TabbedPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TabbedPaneUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class TabbedPaneUITest {
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
        runOnEDT(TabbedPaneUITest::testWhiteBox);
        runOnEDT(TabbedPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.TabbedPaneUI");
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
        javax.swing.JTabbedPane tp = new javax.swing.JTabbedPane();
        tp.addTab("A", new javax.swing.JLabel());
        tp.addTab("B", new javax.swing.JLabel());
        javax.swing.plaf.TabbedPaneUI ui = (javax.swing.plaf.TabbedPaneUI) tp.getUI();
        check(ui != null, "tabbedpane UI non-null");
        check(ui.tabForCoordinate(tp, 0, 0) >= -1, "tabForCoordinate");
        check(ui.getTabBounds(tp, 0) != null, "getTabBounds");
        check(ui.getTabRunCount(tp) >= 1, "tab run count >= 1");
    }
}
