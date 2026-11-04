/*
 * @test
 * @summary javax.swing.plaf.basic.BasicTabbedPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicTabbedPaneUITest
 */
import java.lang.reflect.Modifier;

public class BasicTabbedPaneUITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            java.awt.EventQueue.invokeAndWait(() -> {
                try { r.run(); } catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(BasicTabbedPaneUITest::testWhiteBox);
        runOnEDT(BasicTabbedPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTabbedPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.TabbedPaneUI"), "superclass javax.swing.plaf.TabbedPaneUI");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");

        check(hasDeclaredMethod(c, "createUI"), "declares method createUI");
        check(hasDeclaredMethod(c, "installUI"), "declares method installUI");
        check(hasDeclaredMethod(c, "uninstallUI"), "declares method uninstallUI");
        check(hasDeclaredMethod(c, "getBaseline"), "declares method getBaseline");
        check(hasDeclaredMethod(c, "getMinimumSize"), "declares method getMinimumSize");
        check(hasDeclaredMethod(c, "getMaximumSize"), "declares method getMaximumSize");
    }

    static void testBlackBox() throws Exception {
        var c = new javax.swing.JTabbedPane();
        javax.swing.plaf.basic.BasicTabbedPaneUI ui = new javax.swing.plaf.basic.BasicTabbedPaneUI();
        c.setUI(ui);
        check(c.getUI() == ui, "installed");
        check(ui.getPreferredSize(c) == null, "getPreferredSize falls back to null");
        ui.getMinimumSize(c);
        ui.getMaximumSize(c);
        check(ui.getBaseline(c, 100, 40) >= -1, "getBaseline >= -1");        c.setUI(null);
        javax.swing.plaf.ComponentUI cu = javax.swing.plaf.basic.BasicTabbedPaneUI.createUI(c);
        check(cu != null && cu.getClass() == javax.swing.plaf.basic.BasicTabbedPaneUI.class, "createUI returns BasicTabbedPaneUI");
    }

}
