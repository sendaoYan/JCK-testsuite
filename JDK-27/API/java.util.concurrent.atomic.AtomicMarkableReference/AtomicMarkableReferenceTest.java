/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicMarkableReference conformance: white-box structure
 *          plus black-box behavior of reference/mark accessors, set, compareAndSet (reference
 *          identity + mark), weakCompareAndSet, and attemptMark.
 * @run main AtomicMarkableReferenceTest
 */

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AtomicMarkableReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicMarkableReference");
        check(!c.isInterface(), "AtomicMarkableReference is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicMarkableReference is concrete");
        check(c.getSuperclass() == Object.class, "AtomicMarkableReference extends Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "AtomicMarkableReference is not Serializable");
        c.getConstructor(Object.class, boolean.class);
        c.getMethod("getReference");
        c.getMethod("isMarked");
        c.getMethod("get", boolean[].class);
        c.getMethod("compareAndSet", Object.class, Object.class, boolean.class, boolean.class);
        c.getMethod("attemptMark", Object.class, boolean.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AtomicMarkableReference<String> ref = new AtomicMarkableReference<>("init", false);
        check("init".equals(ref.getReference()), "constructor sets reference");
        check(!ref.isMarked(), "constructor sets mark false");

        boolean[] mark = new boolean[1];
        check("init".equals(ref.get(mark)), "get returns reference");
        check(!mark[0], "get fills mark holder with false");

        ref.set("new", true);
        check("new".equals(ref.getReference()), "set updates reference");
        check(ref.isMarked(), "set updates mark");

        // compareAndSet success when both reference (identity) and mark match
        check(ref.compareAndSet("new", "done", true, false), "compareAndSet success");
        check("done".equals(ref.getReference()) && !ref.isMarked(), "compareAndSet applied reference and mark");

        // compareAndSet failure on wrong reference -> mark and reference unchanged
        check(!ref.compareAndSet("wrong", "x", false, true), "compareAndSet wrong reference returns false");
        check("done".equals(ref.getReference()) && !ref.isMarked(), "wrong reference leaves state unchanged");

        // compareAndSet failure on wrong mark -> unchanged
        check(!ref.compareAndSet("done", "x", true, true), "compareAndSet wrong mark returns false");
        check("done".equals(ref.getReference()) && !ref.isMarked(), "wrong mark leaves state unchanged");

        // weakCompareAndSet success
        check(ref.weakCompareAndSet("done", "weak", false, true), "weakCompareAndSet success");
        check("weak".equals(ref.getReference()) && ref.isMarked(), "weakCompareAndSet applied");
        // weakCompareAndSet failure on stale reference
        check(!ref.weakCompareAndSet("done", "x", true, true), "weakCompareAndSet stale reference returns false");

        // attemptMark: changes mark iff reference matches, reference untouched
        ref.set("same", false);
        check(ref.attemptMark("same", true), "attemptMark success on matching reference");
        check("same".equals(ref.getReference()) && ref.isMarked(), "attemptMark changes only mark");
        check(!ref.attemptMark("other", false), "attemptMark failure on non-matching reference");
        check(ref.isMarked(), "failed attemptMark leaves mark unchanged");

        // mark updates are independent of reference identity
        ref.set("a", false);
        ref.attemptMark("a", true);
        check("a".equals(ref.getReference()) && ref.isMarked(), "attemptMark toggles mark without touching reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
