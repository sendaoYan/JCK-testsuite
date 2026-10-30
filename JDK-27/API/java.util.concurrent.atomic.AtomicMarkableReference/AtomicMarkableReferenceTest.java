/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicMarkableReference API conformance: reference
 *          and mark accessors and compareAndSet.
 * @run main AtomicMarkableReferenceTest
 */

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AtomicMarkableReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicMarkableReference() {
        AtomicMarkableReference<String> ref = new AtomicMarkableReference<>("init", false);
        check("init".equals(ref.getReference()), "getReference");
        check(!ref.isMarked(), "isMarked false");
        ref.set("new", true);
        check("new".equals(ref.getReference()), "getReference after set");
        check(ref.isMarked(), "isMarked true");

        boolean[] mark = new boolean[1];
        check("new".equals(ref.get(mark)), "get with mark holder");
        check(mark[0], "mark holder true");

        boolean cas = ref.compareAndSet("new", "done", true, false);
        check(cas, "compareAndSet");
        check("done".equals(ref.getReference()), "reference after CAS");
        check(!ref.isMarked(), "mark after CAS");
    }

    public static void main(String[] args) {
        testAtomicMarkableReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
