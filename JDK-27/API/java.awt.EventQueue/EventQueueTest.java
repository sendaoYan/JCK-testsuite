/*
 * @test
 * @summary java.awt.EventQueue conformance: white-box structure + black-box behavior.
 * @run main EventQueueTest
 */

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EventQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.EventQueue");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "EventQueue is public");
        check(!Modifier.isAbstract(m), "EventQueue is concrete");
        check(c.getSuperclass() == Object.class, "EventQueue extends Object");
        check(c.getConstructor() != null, "has public no-arg ctor");
        check(Modifier.isPublic(c.getMethod("peekEvent").getModifiers()), "peekEvent public");
        check(Modifier.isPublic(c.getMethod("postEvent", AWTEvent.class).getModifiers()), "postEvent public");
        check(Modifier.isStatic(c.getMethod("invokeLater", Runnable.class).getModifiers()), "invokeLater static");
        check(Modifier.isStatic(c.getMethod("invokeAndWait", Runnable.class).getModifiers()), "invokeAndWait static");
    }

    static void testBlackBox() throws Exception {
        // Fresh queue, empty, non-dispatching thread.
        EventQueue q = new EventQueue();
        check(q.peekEvent() == null, "empty peekEvent null");
        check(q.peekEvent(ActionEvent.ACTION_PERFORMED) == null, "empty peekEvent(id) null");
        check(!EventQueue.isDispatchThread(), "main thread is not dispatch thread");
        check(EventQueue.getCurrentEvent() == null, "getCurrentEvent null off dispatch thread");
        check(EventQueue.getMostRecentEventTime() >= 0, "getMostRecentEventTime non-negative");

        // invokeLater runs on the dispatch thread.
        final CountDownLatch done = new CountDownLatch(1);
        final boolean[] onEdt = new boolean[1];
        EventQueue.invokeLater(new Runnable() { public void run() { onEdt[0] = EventQueue.isDispatchThread(); done.countDown(); } });
        check(done.await(10, TimeUnit.SECONDS), "invokeLater completes");
        check(onEdt[0], "invokeLater ran on dispatch thread");

        // invokeLater preserves FIFO order.
        final java.util.List<Integer> order = Collections.synchronizedList(new ArrayList<Integer>());
        final CountDownLatch both = new CountDownLatch(2);
        EventQueue.invokeLater(new Runnable() { public void run() { order.add(1); both.countDown(); } });
        EventQueue.invokeLater(new Runnable() { public void run() { order.add(2); both.countDown(); } });
        check(both.await(10, TimeUnit.SECONDS), "invokeLater pair completes");
        check(order.size() == 2 && order.get(0) == 1 && order.get(1) == 2, "invokeLater FIFO order");

        // invokeAndWait returns only after the runnable ran on the EDT.
        final boolean[] iwEdt = new boolean[1];
        EventQueue.invokeAndWait(new Runnable() { public void run() { iwEdt[0] = EventQueue.isDispatchThread(); } });
        check(iwEdt[0], "invokeAndWait ran on dispatch thread");

        // invokeAndWait propagates the cause wrapped in InvocationTargetException.
        boolean ite = false;
        try {
            EventQueue.invokeAndWait(new Runnable() { public void run() { throw new IllegalStateException("boom"); } });
        } catch (InvocationTargetException x) {
            ite = (x.getCause() instanceof IllegalStateException);
        }
        check(ite, "invokeAndWait wraps cause in InvocationTargetException");

        // invokeAndWait from the dispatch thread throws Error.
        final boolean[] err = new boolean[1];
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            try { EventQueue.invokeAndWait(new Runnable() { public void run() { } }); }
            catch (Error e) { err[0] = true; }
            catch (Throwable t) { err[0] = false; }
        }});
        check(err[0], "invokeAndWait from dispatch thread throws Error");

        // postEvent/peekEvent/getNextEvent on the system queue, synchronously on the EDT.
        final EventQueue sysQ = Toolkit.getDefaultToolkit().getSystemEventQueue();
        final boolean[] ok = new boolean[1];
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            ActionEvent ev = new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "cmd", 12345L, 0);
            sysQ.postEvent(ev);
            AWTEvent p = sysQ.peekEvent();
            ok[0] = (p != null && p.getID() == ActionEvent.ACTION_PERFORMED);
            AWTEvent p2 = sysQ.peekEvent(ActionEvent.ACTION_PERFORMED);
            ok[0] = ok[0] && (p2 != null);
            AWTEvent g = null;
            try { g = sysQ.getNextEvent(); } catch (InterruptedException ie) { }
            ok[0] = ok[0] && (g != null && g.getID() == ActionEvent.ACTION_PERFORMED);
        }});
        check(ok[0], "postEvent/peekEvent/getNextEvent on system queue");

        // postEvent to the system queue dispatches an ActiveEvent via its dispatch().
        final boolean[] activeRan = new boolean[1];
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            EventQueue.invokeLater(new Runnable() { public void run() { activeRan[0] = true; } });
        }});
        // Drain the queued runnable with another invokeAndWait so ordering is deterministic.
        EventQueue.invokeAndWait(new Runnable() { public void run() { } });
        check(activeRan[0], "invokeLater runnable eventually dispatched");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
