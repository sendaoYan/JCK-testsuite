/*
 * @test
 * @summary javax.swing.Timer conformance: timing config, validation, firing semantics (headless-safe).
 * @run main TimerTest
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class TimerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static void testConfig() {
        Timer t = new Timer(150, null);
        check(t.getDelay() == 150, "delay");
        check(t.getInitialDelay() == 150, "initial delay");
        check(t.isRepeats(), "repeats");
        t.setDelay(200);
        check(t.getDelay() == 200, "setDelay");
        t.setInitialDelay(300);
        check(t.getInitialDelay() == 300, "setInitialDelay");
        check(t.isCoalesce(), "coalesce default true");
        t.setCoalesce(false);
        check(!t.isCoalesce(), "setCoalesce");
        t.setRepeats(false);
        check(!t.isRepeats(), "setRepeats(false)");
        check(t.getActionCommand() == null, "actionCommand default null");
        t.setActionCommand("tick");
        check("tick".equals(t.getActionCommand()), "setActionCommand");
        check(!t.isRunning(), "not running before start");
        check(t.getActionListeners().length == 0, "no action listeners initially");
        check(t.toString() != null && !t.toString().isEmpty(), "toString nonempty");
        check(!Timer.getLogTimers(), "logTimers default false");
        Timer.setLogTimers(true);
        check(Timer.getLogTimers(), "setLogTimers(true)");
        Timer.setLogTimers(false);
        check(!Timer.getLogTimers(), "setLogTimers(false)");
    }

    static void testValidation() {
        new Timer(0, null); // constructor does not validate
        Timer neg = new Timer(-1, null);
        check(neg.getDelay() == -1, "negative delay accepted by constructor");
        Timer z = new Timer(0, null);
        z.setDelay(0);
        check(z.getDelay() == 0, "setDelay(0) allowed");
        checkThrows(IllegalArgumentException.class, () -> z.setDelay(-5));
        checkThrows(IllegalArgumentException.class, () -> z.setInitialDelay(-5));
        check(z.getDelay() == 0, "state intact after rejected setDelay");
        check(z.getInitialDelay() == 0, "initialDelay intact after rejected setInitialDelay");
    }

    static void testFiring() throws Exception {
        final Timer t = new Timer(10, null);
        t.setActionCommand("cmd");
        final CountDownLatch first = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        final boolean[] evt = new boolean[4]; // when>0, source, command, onEdt
        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                evt[0] |= e.getWhen() > 0;
                evt[1] |= e.getSource() == t;
                evt[2] |= "cmd".equals(e.getActionCommand());
                evt[3] |= javax.swing.SwingUtilities.isEventDispatchThread();
                count.incrementAndGet();
                first.countDown();
            }
        };
        t.addActionListener(l);
        check(t.getActionListeners().length == 1, "action listener added");
        t.start();
        check(t.isRunning(), "isRunning after start");
        t.start(); // double start is a no-op, no throw
        check(t.isRunning(), "still running after double start");
        check(first.await(5, TimeUnit.SECONDS), "timer fired within 5s");
        check(count.get() >= 1, "listener notified");
        check(evt[0], "ActionEvent.when > 0");
        check(evt[1], "ActionEvent.source is the Timer");
        check(evt[2], "ActionEvent.actionCommand roundtrip");
        check(evt[3], "events delivered on EDT");
        t.stop();
        check(!t.isRunning(), "not running after stop");
        int after = count.get();
        t.removeActionListener(l);
        t.restart();
        check(t.isRunning(), "restart re-enables running");
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(500);
        while (System.nanoTime() < deadline && count.get() == after)
            Thread.sleep(20);
        check(count.get() == after, "removed listener receives no further events");
        final CountDownLatch again = new CountDownLatch(1);
        ActionListener l2 = new ActionListener() {
            public void actionPerformed(ActionEvent e) { again.countDown(); }
        };
        t.addActionListener(l2);
        t.restart();
        check(again.await(5, TimeUnit.SECONDS), "restart fires again for new listener");
        t.stop();
    }

    static void testInitialDelayFirst() throws Exception {
        Timer t = new Timer(10000, null);
        t.setInitialDelay(10);
        final CountDownLatch fired = new CountDownLatch(1);
        t.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { fired.countDown(); }
        });
        long t0 = System.nanoTime();
        t.start();
        check(fired.await(5, TimeUnit.SECONDS), "first tick honors initialDelay");
        long ms = (System.nanoTime() - t0) / 1_000_000;
        check(ms < 5000, "first tick arrived long before the 10s repeat delay");
        t.stop();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testConfig();
        testValidation();
        testFiring();
        testInitialDelayFirst();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
