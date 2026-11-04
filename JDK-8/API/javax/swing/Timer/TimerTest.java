/*
 * @test
 * @summary javax.swing.Timer conformance: timing config (headless-safe).
 * @run main TimerTest
 */

import javax.swing.Timer;

public class TimerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        Timer t = new Timer(150, null);
        check(t.getDelay() == 150, "delay");
        check(t.getInitialDelay() == 150, "initial delay");
        check(t.isRepeats(), "repeats");
        t.setDelay(200);
        check(t.getDelay() == 200, "setDelay");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
