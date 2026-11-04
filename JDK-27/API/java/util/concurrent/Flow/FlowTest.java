/*
 * @test
 * @summary java.util.concurrent.Flow conformance: white-box structure + black-box behavior
 *          (defaultBufferSize exact value; nested interface relationships; reactive-streams
 *          callback contract closed loop with a hand-written synchronous publisher).
 * @run main FlowTest
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Flow");
        check(!c.isInterface(), "Flow is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Flow is final");
        hasMethod(c, "defaultBufferSize");
        check(Flow.Publisher.class.isInterface(), "Publisher is an interface");
        check(Flow.Subscriber.class.isInterface(), "Subscriber is an interface");
        check(Flow.Subscription.class.isInterface(), "Subscription is an interface");
        check(Flow.Processor.class.isInterface(), "Processor is an interface");
        check(Flow.Processor.class.getInterfaces().length == 2, "Processor directly extends two interfaces");
        check(java.util.Arrays.asList(Flow.Processor.class.getInterfaces())
                  .containsAll(java.util.Arrays.asList(Flow.Subscriber.class, Flow.Publisher.class)),
                "Processor extends Subscriber and Publisher");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Flow.defaultBufferSize() > 0, "defaultBufferSize() > 0");
        // javadoc: the default buffer size is currently 256; assert the exact locked value
        check(Flow.defaultBufferSize() == 256, "defaultBufferSize() == 256 (javadoc-locked)");
        check(Flow.defaultBufferSize() == Flow.defaultBufferSize(), "defaultBufferSize() is stable across calls");
    }

    // ---- Part 3: reactive-streams contract closed loop ----
    // A hand-written, fully synchronous Publisher implementing the Flow contract:
    // onSubscribe first, then onNext per accumulated demand, then exactly one terminal signal.
    static final class SyncPublisher implements Flow.Publisher<Integer> {
        final java.util.List<Integer> items;
        SyncPublisher(java.util.List<Integer> items) { this.items = items; }
        public void subscribe(Flow.Subscriber<? super Integer> s) {
            java.util.List<Object> log = new java.util.ArrayList<>();
            Flow.Subscription sub = new Flow.Subscription() {
                long demand;
                int cursor;
                boolean done;
                public void request(long n) {
                    if (n <= 0) {
                        s.onError(new IllegalArgumentException("3.9: non-positive requests"));
                        done = true;
                        return;
                    }
                    demand += n;
                    while (cursor < items.size() && demand > 0 && !done) {
                        log.add("onNext:" + items.get(cursor));
                        s.onNext(items.get(cursor));
                        cursor++;
                        demand--;
                    }
                    if (cursor == items.size() && !done) {
                        done = true;
                        log.add("onComplete");
                        s.onComplete();
                    }
                }
                public void cancel() { done = true; }
            };
            log.add("onSubscribe");
            s.onSubscribe(sub);
        }
    }

    static final class RecordingSub implements Flow.Subscriber<Integer> {
        final java.util.List<Object> log = new java.util.concurrent.CopyOnWriteArrayList<>();
        volatile Flow.Subscription subscription;
        public void onSubscribe(Flow.Subscription s) { subscription = s; log.add("onSubscribe"); }
        public void onNext(Integer i) { log.add("onNext:" + i); }
        public void onError(Throwable t) { log.add("onError:" + t.getClass().getSimpleName()); }
        public void onComplete() { log.add("onComplete"); }
    }

    static void testCallbackContract() {
        // onSubscribe strictly first, then onNext x N in order, then onComplete exactly once
        RecordingSub sub = new RecordingSub();
        new SyncPublisher(java.util.Arrays.asList(1, 2, 3)).subscribe(sub);
        sub.subscription.request(10);
        check(sub.log.size() == 5, "5 signals total (1 subscribe + 3 onNext + 1 complete): " + sub.log);
        check("onSubscribe".equals(String.valueOf(sub.log.get(0))), "onSubscribe is the first signal");
        check("onNext:1".equals(String.valueOf(sub.log.get(1)))
                && "onNext:2".equals(String.valueOf(sub.log.get(2)))
                && "onNext:3".equals(String.valueOf(sub.log.get(3))),
                "onNext events arrive in submission order");
        check("onComplete".equals(String.valueOf(sub.log.get(4))), "onComplete is the terminal signal");
        check(sub.log.stream().filter(o -> "onComplete".equals(String.valueOf(o))).count() == 1,
                "exactly one terminal signal");

        // incremental request: demand accumulates, unrequested items are withheld
        RecordingSub sub2 = new RecordingSub();
        new SyncPublisher(java.util.Arrays.asList(1, 2)).subscribe(sub2);
        sub2.subscription.request(1);
        check(sub2.log.size() == 2 && "onNext:1".equals(String.valueOf(sub2.log.get(1))),
                "only one item delivered for request(1)");
        sub2.subscription.request(1);
        check(sub2.log.size() == 4, "second request delivers the second item then completes: " + sub2.log);

        // request(0)/negative: rule 3.9 -> subscriber receives onError(IllegalArgumentException)
        RecordingSub sub3 = new RecordingSub();
        new SyncPublisher(java.util.Arrays.asList(1)).subscribe(sub3);
        sub3.subscription.request(0);
        check(sub3.log.size() == 2 && "onError:IllegalArgumentException".equals(String.valueOf(sub3.log.get(1))),
                "request(0) yields onError(IllegalArgumentException): " + sub3.log);
        RecordingSub sub4 = new RecordingSub();
        new SyncPublisher(java.util.Arrays.asList(1)).subscribe(sub4);
        sub4.subscription.request(-3);
        check(sub4.log.size() == 2 && "onError:IllegalArgumentException".equals(String.valueOf(sub4.log.get(1))),
                "request(-3) yields onError(IllegalArgumentException): " + sub4.log);

        // cancel: no further onNext after cancel
        RecordingSub sub5 = new RecordingSub();
        new SyncPublisher(java.util.Arrays.asList(1, 2, 3)).subscribe(sub5);
        sub5.subscription.request(1);
        sub5.subscription.cancel();
        sub5.subscription.request(10);
        check(sub5.log.size() == 2, "no signals after cancel: " + sub5.log);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCallbackContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
