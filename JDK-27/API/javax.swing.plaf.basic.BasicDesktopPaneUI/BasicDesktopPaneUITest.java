/*
 * @test
 * @summary javax.swing.plaf.basic.BasicDesktopPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicDesktopPaneUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicDesktopPaneUITest {
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
        runOnEDT(BasicDesktopPaneUITest::testWhiteBox);
        runOnEDT(BasicDesktopPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicDesktopPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.DesktopPaneUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JDesktopPane dp = new javax.swing.JDesktopPane();
        javax.swing.plaf.basic.BasicDesktopPaneUI ui = new javax.swing.plaf.basic.BasicDesktopPaneUI();
        check(javax.swing.plaf.basic.BasicDesktopPaneUI.createUI(dp) instanceof javax.swing.plaf.basic.BasicDesktopPaneUI, "createUI");
        dp.setUI(ui);
        check(dp.getUI() == ui, "installed");
        dp.setUI(null);
    }
}
