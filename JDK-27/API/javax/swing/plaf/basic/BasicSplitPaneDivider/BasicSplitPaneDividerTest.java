/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneDivider conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicSplitPaneDividerTest
 */
import java.lang.reflect.Modifier;

public class BasicSplitPaneDividerTest {
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
        runOnEDT(BasicSplitPaneDividerTest::testWhiteBox);
        runOnEDT(BasicSplitPaneDividerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneDivider");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass java.awt.Container");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.beans.PropertyChangeListener"), "interface 0");

        check(hasDeclaredMethod(c, "getBasicSplitPaneUI"), "declares method getBasicSplitPaneUI");
        check(hasDeclaredMethod(c, "setBasicSplitPaneUI"), "declares method setBasicSplitPaneUI");
        check(hasDeclaredMethod(c, "setDividerSize"), "declares method setDividerSize");
        check(hasDeclaredMethod(c, "getDividerSize"), "declares method getDividerSize");
        check(hasDeclaredMethod(c, "isMouseOver"), "declares method isMouseOver");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JSplitPane sp = new javax.swing.JSplitPane();
        javax.swing.plaf.basic.BasicSplitPaneUI ui = new javax.swing.plaf.basic.BasicSplitPaneUI();
        ui.installUI(sp);
        javax.swing.plaf.basic.BasicSplitPaneDivider d = new javax.swing.plaf.basic.BasicSplitPaneDivider(ui);
        check(d.getBasicSplitPaneUI() == ui, "getBasicSplitPaneUI round-trip");
        d.setDividerSize(11);
        check(d.getDividerSize() == 11, "setDividerSize/getDividerSize round-trip");
        check(d.isMouseOver() == false, "not mouse-over initially");
        check(d.getMinimumSize() != null, "minimum size non-null");
        check(d.getPreferredSize() != null, "preferred size non-null");
        check(d.getInsets() != null, "insets non-null");
    }

}
