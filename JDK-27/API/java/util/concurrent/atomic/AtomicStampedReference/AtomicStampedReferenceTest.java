/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicStampedReference conformance: white-box structure
 *          plus black-box behavior of reference/stamp accessors, set, compareAndSet (reference
 *          identity + stamp), weakCompareAndSet, and attemptStamp.
 * @run main AtomicStampedReferenceTest
 */

import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicStampedReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.AtomicStampedReference");
        check(!c.isInterface(), "AtomicStampedReference is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AtomicStampedReference is concrete");
        check(c.getSuperclass() == Object.class, "AtomicStampedReference extends Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "AtomicStampedReference is not Serializable");
        c.getConstructor(Object.class, int.class);
        c.getMethod("getReference");
        c.getMethod("getStamp");
        c.getMethod("get", int[].class);
        c.getMethod("compareAndSet", Object.class, Object.class, int.class, int.class);
        c.getMethod("attemptStamp", Object.class, int.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AtomicStampedReference<String> ref = new AtomicStampedReference<>("init", 100);
        check("init".equals(ref.getReference()), "constructor sets reference");
        check(ref.getStamp() == 100, "constructor sets stamp");

        int[] stamp = new int[1];
        check("init".equals(ref.get(stamp)), "get returns reference");
        check(stamp[0] == 100, "get fills stamp holder");

        ref.set("new", 101);
        check("new".equals(ref.getReference()) && ref.getStamp() == 101, "set updates reference and stamp");

        // compareAndSet success when reference (identity) and stamp match
        check(ref.compareAndSet("new", "done", 101, 102), "compareAndSet success");
        check("done".equals(ref.getReference()) && ref.getStamp() == 102, "compareAndSet applied reference and stamp");

        // failure on wrong reference
        check(!ref.compareAndSet("wrong", "x", 102, 103), "compareAndSet wrong reference returns false");
        check("done".equals(ref.getReference()) && ref.getStamp() == 102, "wrong reference leaves state unchanged");

        // failure on wrong stamp
        check(!ref.compareAndSet("done", "x", 999, 103), "compareAndSet wrong stamp returns false");
        check("done".equals(ref.getReference()) && ref.getStamp() == 102, "wrong stamp leaves state unchanged");

        // weakCompareAndSet
        check(ref.weakCompareAndSet("done", "weak", 102, 103), "weakCompareAndSet success");
        check("weak".equals(ref.getReference()) && ref.getStamp() == 103, "weakCompareAndSet applied");
        check(!ref.weakCompareAndSet("done", "x", 103, 104), "weakCompareAndSet stale reference returns false");

        // attemptStamp: changes stamp iff reference matches, reference untouched
        ref.set("same", 200);
        check(ref.attemptStamp("same", 201), "attemptStamp success on matching reference");
        check("same".equals(ref.getReference()) && ref.getStamp() == 201, "attemptStamp changes only stamp");
        check(!ref.attemptStamp("other", 202), "attemptStamp failure on non-matching reference");
        check(ref.getStamp() == 201, "failed attemptStamp leaves stamp unchanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
