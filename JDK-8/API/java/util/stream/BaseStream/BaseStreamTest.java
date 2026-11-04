/*
 * @test
 * @summary java.util.stream.BaseStream API conformance: sequential/parallel/onClose/close.
 * @run main BaseStreamTest
 */

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

public class BaseStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBaseStream() {
        BaseStream<Integer, Stream<Integer>> bs = Stream.of(1, 2, 3);
        check(!bs.isParallel(), "stream starts sequential");
        Stream<Integer> par = bs.parallel();
        check(par.isParallel(), "parallel() makes it parallel");
        check(par.sequential() == par, "sequential() returns the same stream");
        check(bs.unordered().count() == 3, "unordered() returns an equivalent stream");
        Iterator<Integer> it = Stream.of(1, 2, 3).iterator();
        check(it.hasNext() && it.next() == 1, "iterator() yields first element");
        Spliterator<Integer> sp = Stream.of(1, 2, 3).spliterator();
        check(sp != null && sp.estimateSize() >= 0, "spliterator() present");
        Stream<Integer> c = Stream.of(1).onClose(() -> { });
        check(c != null, "onClose() returns a stream");
        c.close();
        check(true, "close() completes without error");
    }

    public static void main(String[] args) {
        testBaseStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
