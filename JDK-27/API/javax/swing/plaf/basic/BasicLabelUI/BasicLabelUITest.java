/*
 * @test
 * @summary javax.swing.plaf.basic.BasicLabelUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicLabelUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicLabelUITest {
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
        runOnEDT(BasicLabelUITest::testWhiteBox);
        runOnEDT(BasicLabelUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicLabelUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.LabelUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.beans.PropertyChangeListener"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JLabel l = new javax.swing.JLabel("X");
        javax.swing.plaf.basic.BasicLabelUI ui = new javax.swing.plaf.basic.BasicLabelUI();
        check(javax.swing.plaf.basic.BasicLabelUI.createUI(l) instanceof javax.swing.plaf.basic.BasicLabelUI, "createUI");
        l.setUI(ui);
        check(l.getUI() == ui, "installed");
        check(ui.getPreferredSize(l) != null, "preferred");
        check(ui.getMinimumSize(l) != null, "minimum");
        check(ui.getMaximumSize(l) != null, "maximum");
        check(ui.getBaseline(l, 100, 30) >= -1, "baseline");
        l.setUI(null);
    }
}
