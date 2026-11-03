/*
 * @test
 * @summary javax.swing.plaf.basic.BasicButtonUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicButtonUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicButtonUITest {
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
        runOnEDT(BasicButtonUITest::testWhiteBox);
        runOnEDT(BasicButtonUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicButtonUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ButtonUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JButton b = new javax.swing.JButton("X");
        javax.swing.plaf.basic.BasicButtonUI ui = new javax.swing.plaf.basic.BasicButtonUI();
        check(javax.swing.plaf.basic.BasicButtonUI.createUI(b) instanceof javax.swing.plaf.basic.BasicButtonUI, "createUI");
        b.setUI(ui);
        check(b.getUI() == ui, "setUI installs and sets UI");
        check(ui.getPreferredSize(b) != null, "preferred");
        check(ui.getMinimumSize(b) != null, "minimum");
        check(ui.getMaximumSize(b) != null, "maximum");
        check(ui.getDefaultTextIconGap(b) >= 0, "text icon gap");
        check(ui.getBaseline(b, 100, 30) >= -1, "baseline");
        b.setUI(null);
        check(b.getUI() == null, "setUI(null) clears UI");
    }
}
