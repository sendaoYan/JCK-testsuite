/*
 * @test
 * @summary javax.swing.SwingWorker conformance: white-box structure + black-box behavior.
 * @run main SwingWorkerTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

public class SwingWorkerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingWorker");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.concurrent.RunnableFuture"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SwingWorker<Integer, String> w = new SwingWorker<Integer, String>() {
            protected Integer doInBackground() { return 42; }
        };
        check(w.getProgress() == 0, "initial progress");
        check(!w.isDone(), "not done initially");
        check(w.getPropertyChangeSupport() != null, "property change support");
        w.execute();
        check(w.get(5, TimeUnit.SECONDS) == 42, "get result");
        check(w.isDone(), "isDone");
        check(w.getState() == SwingWorker.StateValue.DONE, "state DONE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
