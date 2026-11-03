/*
 * @test
 * @summary javax.swing.plaf.basic.BasicProgressBarUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicProgressBarUITest
 */
import java.lang.reflect.Modifier;

public class BasicProgressBarUITest {
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
        runOnEDT(BasicProgressBarUITest::testWhiteBox);
        runOnEDT(BasicProgressBarUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicProgressBarUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ProgressBarUI"), "superclass javax.swing.plaf.ProgressBarUI");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "createUI"), "declares method createUI");
        check(hasDeclaredMethod(c, "installUI"), "declares method installUI");
        check(hasDeclaredMethod(c, "uninstallUI"), "declares method uninstallUI");
        check(hasDeclaredMethod(c, "getBaseline"), "declares method getBaseline");
        check(hasDeclaredMethod(c, "paint"), "declares method paint");
        check(hasDeclaredMethod(c, "getPreferredSize"), "declares method getPreferredSize");
    }

    static void testBlackBox() throws Exception {
        var c = new javax.swing.JProgressBar();
        javax.swing.plaf.basic.BasicProgressBarUI ui = new javax.swing.plaf.basic.BasicProgressBarUI();
        c.setUI(ui);
        check(c.getUI() == ui, "installed");
        check(ui.getPreferredSize(c) != null, "getPreferredSize non-null");
        ui.getMinimumSize(c);
        ui.getMaximumSize(c);
        check(ui.getBaseline(c, 100, 40) >= -1, "getBaseline >= -1");        c.setUI(null);
        javax.swing.plaf.ComponentUI cu = javax.swing.plaf.basic.BasicProgressBarUI.createUI(c);
        check(cu != null && cu.getClass() == javax.swing.plaf.basic.BasicProgressBarUI.class, "createUI returns BasicProgressBarUI");
    }

}
