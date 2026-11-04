/*
 * @test
 * @summary java.util.stream.BaseStream conformance: white-box structure + black-box behavior.
 * @run main BaseStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void expectThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.BaseStream");
        check(c.isInterface(), "BaseStream is an interface");
        check(Modifier.isPublic(c.getModifiers()), "BaseStream is public");
        boolean extAuto = false;
        for (Class<?> i : c.getInterfaces()) if (i == AutoCloseable.class) extAuto = true;
        check(extAuto, "BaseStream extends AutoCloseable");
        String[] names = {"iterator", "spliterator", "isParallel", "sequential",
                          "parallel", "unordered", "onClose", "close"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares " + n + "()");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // sequential / parallel / isParallel
        BaseStream<Integer, Stream<Integer>> bs = Stream.of(1, 2, 3);
        check(!bs.isParallel(), "stream starts sequential");
        Stream<Integer> par = bs.parallel();
        check(par.isParallel(), "parallel() makes it parallel");
        check(par.sequential() == par, "sequential() returns the same stream");
        check(!par.isParallel(), "sequential() makes it sequential again");

        // unordered() returns an equivalent stream (content preserved)
        List<Integer> unordered = bs.unordered().collect(Collectors.toList());
        check(unordered.equals(List.of(1, 2, 3)), "unordered() preserves content");

        // iterator / spliterator
        Iterator<Integer> it = Stream.of(1, 2, 3).iterator();
        check(it.hasNext() && it.next() == 1, "iterator() yields first element");
        Spliterator<Integer> sp = Stream.of(1, 2, 3).spliterator();
        check(sp != null && sp.estimateSize() == 3, "spliterator() has sized estimate");

        // onClose / close: handler runs exactly once on close, later close is a no-op
        AtomicInteger closed = new AtomicInteger();
        BaseStream<Integer, Stream<Integer>> c = Stream.of(1, 2, 3).onClose(closed::incrementAndGet);
        check(closed.get() == 0, "handler not run before close");
        c.close();
        check(closed.get() == 1, "close() runs handler once");
        c.close();
        check(closed.get() == 1, "second close() does not re-run handler");

        // multiple onClose handlers all run
        AtomicInteger multi = new AtomicInteger();
        BaseStream<Integer, Stream<Integer>> m = Stream.of(1)
                .onClose(multi::incrementAndGet)
                .onClose(multi::incrementAndGet);
        m.close();
        check(multi.get() == 2, "all onClose handlers run");

        // parallel stream still yields all elements in a deterministic total
        List<Integer> got = new ArrayList<>();
        Stream.of(1, 2, 3).parallel().forEach(got::add);
        Collections.sort(got);
        check(got.equals(List.of(1, 2, 3)), "parallel forEach visits all elements");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
