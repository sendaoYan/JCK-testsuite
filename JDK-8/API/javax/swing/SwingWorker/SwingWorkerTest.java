/*
 * @test
 * @summary javax.swing.SwingWorker conformance: type shape + lifecycle/process/cancel semantics (headless-safe).
 * @run main SwingWorkerTest
 */

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SwingWorkerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingWorker", false, SwingWorkerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.util.concurrent.RunnableFuture"), "implements java.util.concurrent.RunnableFuture");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("run"), "method run");
        check(names.contains("getProgress"), "method getProgress");
        check(names.contains("execute"), "method execute");
        check(names.contains("cancel"), "method cancel");
        check(names.contains("isCancelled"), "method isCancelled");
        check(names.contains("isDone"), "method isDone");
        check(names.contains("get"), "method get");
    }

    static class Recorder extends SwingWorker<String, Integer> {
        final List<Integer> processed = Collections.synchronizedList(new ArrayList<Integer>());
        final CountDownLatch doneLatch = new CountDownLatch(1);
        volatile boolean bgOnEdt, doneOnEdt;
        volatile int doneCount;
        @Override protected String doInBackground() {
            bgOnEdt = SwingUtilities.isEventDispatchThread();
            publish(1);
            publish(2, 3);
            setProgress(42);
            return "R";
        }
        @Override protected void process(List<Integer> chunks) { processed.addAll(chunks); }
        @Override protected void done() {
            doneCount++;
            doneOnEdt = SwingUtilities.isEventDispatchThread();
            doneLatch.countDown();
        }
    }

    static void testLifecycle() throws Exception {
        Recorder w = new Recorder();
        check(w.getState() == SwingWorker.StateValue.PENDING, "initial state PENDING");
        check(w.getProgress() == 0, "initial progress 0");
        check(!w.isDone(), "isDone false initially");
        final List<PropertyChangeEvent> evts = Collections.synchronizedList(new ArrayList<PropertyChangeEvent>());
        w.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evts.add(e); }
        });
        w.execute();
        check(w.getState() == SwingWorker.StateValue.PENDING,
                "state still PENDING immediately after execute");
        String r = w.get(10, SECONDS);
        check("R".equals(r), "get returns doInBackground result");
        check(w.doneLatch.await(5, SECONDS), "done ran");
        check(w.isDone(), "isDone after completion");
        check(w.getState() == SwingWorker.StateValue.DONE, "state DONE");
        check(!w.isCancelled(), "not cancelled");
        check(w.doneCount == 1, "done ran exactly once");
        check(w.doneOnEdt, "done() runs on EDT");
        check(!w.bgOnEdt, "doInBackground() runs off EDT");
        check(w.processed.equals(Arrays.asList(1, 2, 3)), "process() accumulates published chunks");
        check(w.getProgress() == 42, "getProgress after setProgress");
        // status 属性变更由 SwingWorkerPropertyChangeSupport 异步投递到 EDT，且 done()
        // 的排队早于 DONE 通知；先排空 EDT 队列，才能断言事件已经投递。
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
        boolean sawStarted = false, sawDone = false, sawProgress = false;
        for (PropertyChangeEvent e : evts) {
            if ("state".equals(e.getPropertyName())) {
                if (e.getNewValue() == SwingWorker.StateValue.STARTED) sawStarted = true;
                if (e.getNewValue() == SwingWorker.StateValue.DONE) sawDone = true;
            } else if ("progress".equals(e.getPropertyName()) && Integer.valueOf(42).equals(e.getNewValue())) {
                sawProgress = true;
            }
        }
        check(sawStarted, "state event PENDING->STARTED fired");
        check(sawDone, "state event STARTED->DONE fired");
        check(sawProgress, "progress event 0->42 fired");
        w.execute(); // second execute is a documented no-op
        Thread.sleep(100);
        check(w.doneCount == 1, "second execute does not rerun the worker");
        check("R".equals(w.get()), "get() after completion is stable");
    }

    static class Stuck extends SwingWorker<Integer, Void> {
        final CountDownLatch block = new CountDownLatch(1);
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(1);
        volatile boolean interrupted;
        @Override protected Integer doInBackground() throws InterruptedException {
            started.countDown();
            try {
                block.await();
                return 7;
            } catch (InterruptedException e) {
                interrupted = true;
                throw e;
            }
        }
        @Override protected void done() { doneLatch.countDown(); }
    }

    static void testCancel() throws Exception {
        Stuck w = new Stuck();
        w.execute();
        check(w.started.await(5, SECONDS), "doInBackground started");
        check(w.cancel(true), "cancel(true) returns true");
        check(w.isCancelled(), "isCancelled after cancel");
        check(w.doneLatch.await(5, SECONDS), "done ran after cancel");
        check(w.isDone(), "isDone after cancel");
        check(w.getState() == SwingWorker.StateValue.DONE, "state DONE after cancel");
        check(w.interrupted, "interrupt delivered into blocking doInBackground");
        checkThrows(CancellationException.class, new Runnable() {
            public void run() {
                try { w.get(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (ExecutionException e) { throw new RuntimeException(e); }
            }
        });
        check(!w.cancel(false), "cancel after done returns false");
    }

    static class ProgressProbe extends SwingWorker<Void, Void> {
        final CountDownLatch doneLatch = new CountDownLatch(1);
        volatile int rejected;
        @Override protected Void doInBackground() {
            try { setProgress(-1); } catch (IllegalArgumentException e) { rejected++; }
            try { setProgress(101); } catch (IllegalArgumentException e) { rejected++; }
            setProgress(0);
            setProgress(100);
            return null;
        }
        @Override protected void done() { doneLatch.countDown(); }
    }

    static void testProgressBounds() throws Exception {
        ProgressProbe w = new ProgressProbe();
        w.execute();
        check(w.doneLatch.await(5, SECONDS), "progress probe finished");
        check(w.rejected == 2, "setProgress(-1)/(101) rejected with IAE");
        check(w.getProgress() == 100, "setProgress(0) and (100) accepted at bounds");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testLifecycle();
        testCancel();
        testProgressBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
