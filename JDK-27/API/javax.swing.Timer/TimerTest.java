/*
 * @test
 * @summary javax.swing.Timer conformance: white-box structure + black-box behavior.
 * @run main TimerTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

public class TimerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Timer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        final int[] count = { 0 };
        Timer t = new Timer(20, e -> { count[0]++; latch.countDown(); });
        check(t.getDelay() == 20, "getDelay");
        check(t.isRepeats(), "repeats default");
        check(!t.isRunning(), "not running initially");

        t.start();
        check(t.isRunning(), "running after start");
        check(latch.await(5, TimeUnit.SECONDS), "fired twice");
        t.stop();
        check(!t.isRunning(), "stopped");
        check(count[0] >= 2, "fired count");

        t.setDelay(100);
        check(t.getDelay() == 100, "setDelay");
        t.setInitialDelay(50);
        check(t.getInitialDelay() == 50, "setInitialDelay");
        t.setRepeats(false);
        check(!t.isRepeats(), "setRepeats false");
        t.setCoalesce(false);
        check(!t.isCoalesce(), "setCoalesce false");
        t.setActionCommand("cmd");
        check("cmd".equals(t.getActionCommand()), "action command");

        Timer.setLogTimers(false);
        check(!Timer.getLogTimers(), "getLogTimers");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
