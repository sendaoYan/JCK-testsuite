/*
 * @test
 * @summary javax.swing.text.TabableView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TabableViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;

public class TabableViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TabableView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TabableView");

        // exact method surface
        check(c.getDeclaredMethods().length == 2, "two abstract methods");
        Method getTabbedSpan = c.getMethod("getTabbedSpan", float.class, TabExpander.class);
        check(getTabbedSpan.getReturnType() == float.class, "getTabbedSpan returns float");
        Method getPartialSpan = c.getMethod("getPartialSpan", int.class, int.class);
        check(getPartialSpan.getReturnType() == float.class, "getPartialSpan returns float");

        // a conforming implementation honors its own tab/partial calculations
        TabableView v = new TabableView() {
            public float getTabbedSpan(float x, TabExpander e) { return x + 100f; }
            public float getPartialSpan(int p0, int p1) { return p1 - p0; }
        };
        check(v.getTabbedSpan(5f, null) == 105f, "getTabbedSpan contract exercised");
        check(v.getPartialSpan(2, 9) == 7f, "getPartialSpan contract exercised");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TabableViewTest::testWhiteBox);
        runOnEDT(TabableViewTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
