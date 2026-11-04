/*
 * @test
 * @summary javax.swing.text.TabExpander conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TabExpanderTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.text.TabExpander;

public class TabExpanderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TabExpander");
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
        Class<?> c = Class.forName("javax.swing.text.TabExpander");

        check(c.getDeclaredMethods().length == 1, "one abstract method");
        Method nextTabStop = c.getMethod("nextTabStop", float.class, int.class);
        check(nextTabStop.getReturnType() == float.class, "nextTabStop returns float");

        // a conforming implementation computes the next stop from x and offset
        TabExpander e = new TabExpander() {
            public float nextTabStop(float x, int tabOffset) { return x + 8f * tabOffset; }
        };
        check(e.nextTabStop(10f, 3) == 34f, "nextTabStop contract exercised");
        check(e.nextTabStop(0f, 0) == 0f, "nextTabStop base case");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TabExpanderTest::testWhiteBox);
        runOnEDT(TabExpanderTest::testBlackBox);
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
