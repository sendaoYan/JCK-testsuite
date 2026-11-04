/*
 * @test
 * @summary javax.swing.plaf.basic.BasicInternalFrameTitlePane.SizeAction conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SizeActionTest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class SizeActionTest {
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
        runOnEDT(SizeActionTest::testWhiteBox);
        runOnEDT(SizeActionTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicInternalFrameTitlePane$SizeAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractAction"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JInternalFrame f = new javax.swing.JInternalFrame("t");
        javax.swing.plaf.basic.BasicInternalFrameTitlePane tp = new javax.swing.plaf.basic.BasicInternalFrameTitlePane(f);
        javax.swing.plaf.basic.BasicInternalFrameTitlePane.SizeAction a = tp.new SizeAction();
        check(a instanceof javax.swing.Action, "is Action");
        a.actionPerformed(new java.awt.event.ActionEvent(f, java.awt.event.ActionEvent.ACTION_PERFORMED, "size"));
        check(true, "actionPerformed ok");
    }
}
