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

    // ---- 契约深化：identity 语义 / 双失配分支 / null 引用分支 / stamp-only CAS ----
    static void testIdentitySemantics() {
        String a1 = new String("a");
        String a2 = new String("a");
        check(a1.equals(a2) && a1 != a2, "precondition: equals but not same reference");
        AtomicStampedReference<String> ref = new AtomicStampedReference<>(a1, 0);
        check(!ref.compareAndSet(a2, "b", 0, 1), "identity CAS with equals-but-not-same ref fails");
        check(ref.getReference() == a1 && ref.getStamp() == 0, "failed identity CAS leaves state unchanged");
        check(!ref.attemptStamp(a2, 5), "attemptStamp with non-matching identity ref fails");
        check(ref.getStamp() == 0, "failed attemptStamp leaves stamp unchanged");
        check(ref.compareAndSet(a1, "b", 0, 1), "identity CAS with == ref succeeds");
    }

    static void testBothMismatchAndNull() {
        // ref 与 stamp 都失配 → false 且不变
        AtomicStampedReference<Integer> ref = new AtomicStampedReference<>(10, 1);
        check(!ref.compareAndSet(99, 30, 9, 3), "both-mismatch CAS returns false");
        check(ref.getReference() == 10 && ref.getStamp() == 1, "both-mismatch leaves state unchanged");
        // newRef == 现值、仅换戳：成功且引用不变
        check(ref.compareAndSet(10, 10, 1, 2), "same-ref stamp-only CAS succeeds");
        check(ref.getReference() == 10 && ref.getStamp() == 2, "stamp-only CAS changes only stamp");
        // newStamp == 现戳、仅换引用
        check(ref.compareAndSet(10, 11, 2, 2), "same-stamp ref-only CAS succeeds");
        check(ref.getReference() == 11 && ref.getStamp() == 2, "ref-only CAS changes only reference");
        // expectedRef 为 null 的分支
        AtomicStampedReference<String> n = new AtomicStampedReference<>(null, 0);
        check(n.compareAndSet(null, "v", 0, 1), "CAS(null, v, 0, 1) on null initial succeeds");
        check(!n.compareAndSet(null, "w", 1, 2), "CAS(null, w) after set fails");
        // attemptStamp 只改戳不动引用
        check(n.attemptStamp("v", 7), "attemptStamp matching ref returns true");
        check("v".equals(n.getReference()) && n.getStamp() == 7, "attemptStamp changed only stamp");
        // set 无条件更新两者
        n.set("next", 10);
        check("next".equals(n.getReference()) && n.getStamp() == 10, "set updates reference and stamp");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIdentitySemantics();
        testBothMismatchAndNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
