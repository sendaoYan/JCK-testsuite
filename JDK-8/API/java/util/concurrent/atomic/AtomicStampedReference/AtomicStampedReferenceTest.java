/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicStampedReference API conformance: reference
 *          and stamp accessors and compareAndSet.
 * @run main AtomicStampedReferenceTest
 */

import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicStampedReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicStampedReference() {
        AtomicStampedReference<Integer> ref = new AtomicStampedReference<>(0, 100);
        check(ref.getReference() == 0, "getReference");
        check(ref.getStamp() == 100, "getStamp");
        ref.set(1, 101);
        check(ref.getReference() == 1 && ref.getStamp() == 101, "set");

        int[] stamp = new int[1];
        check(ref.get(stamp) == 1, "get with stamp holder");
        check(stamp[0] == 101, "stamp holder");

        boolean cas = ref.compareAndSet(1, 2, 101, 102);
        check(cas, "compareAndSet");
        check(ref.getReference() == 2 && ref.getStamp() == 102, "after CAS");
    }

    // ---- 契约深化：四参失配矩阵 / identity 语义 / attemptStamp 两分支 / weakCAS ----
    static void testCasFailureMatrix() {
        // 全匹配 → true
        AtomicStampedReference<Integer> ref = new AtomicStampedReference<>(10, 1);
        check(ref.compareAndSet(10, 20, 1, 2), "all-match CAS succeeds");
        check(ref.getReference() == 20 && ref.getStamp() == 2, "all-match CAS applies ref and stamp");
        // ref 失配 → false 且不变
        check(!ref.compareAndSet(10, 30, 2, 3), "ref-mismatch CAS returns false");
        check(ref.getReference() == 20 && ref.getStamp() == 2, "ref-mismatch leaves state unchanged");
        // stamp 失配 → false 且不变
        check(!ref.compareAndSet(20, 30, 1, 3), "stamp-mismatch CAS returns false");
        check(ref.getReference() == 20 && ref.getStamp() == 2, "stamp-mismatch leaves state unchanged");
        // ref 与 stamp 都失配 → false 且不变
        check(!ref.compareAndSet(10, 30, 1, 3), "both-mismatch CAS returns false");
        check(ref.getReference() == 20 && ref.getStamp() == 2, "both-mismatch leaves state unchanged");
        // newRef == 现值、仅换戳：成功且引用不变
        check(ref.compareAndSet(20, 20, 2, 5), "same-ref stamp-only CAS succeeds");
        check(ref.getReference() == 20 && ref.getStamp() == 5, "stamp-only CAS changes only stamp");
        // expectedRef 为 null 的分支
        AtomicStampedReference<String> n = new AtomicStampedReference<>(null, 0);
        check(n.compareAndSet(null, "v", 0, 1), "CAS(null, v, 0, 1) on null initial succeeds");
        check(!n.compareAndSet(null, "w", 1, 2), "CAS(null, w) after set fails");
    }

    static void testIdentitySemantics() {
        // 引用按 ==（同一性）匹配：equals() 相等的不同对象不匹配
        String a1 = new String("a");
        String a2 = new String("a");
        check(a1.equals(a2) && a1 != a2, "precondition: equals but not same reference");
        AtomicStampedReference<String> ref = new AtomicStampedReference<>(a1, 0);
        check(!ref.compareAndSet(a2, "b", 0, 1), "identity CAS with equals-but-not-same ref fails");
        check(ref.getReference() == a1 && ref.getStamp() == 0, "failed identity CAS leaves state unchanged");
        check(ref.compareAndSet(a1, "b", 0, 1), "identity CAS with == ref succeeds");
    }

    static void testAttemptStamp() {
        String a1 = new String("a");
        String a2 = new String("a");
        AtomicStampedReference<String> ref = new AtomicStampedReference<>(a1, 7);
        // 引用匹配 → 只改戳，返回 true
        check(ref.attemptStamp(a1, 8), "attemptStamp with matching reference returns true");
        check(ref.getStamp() == 8, "attemptStamp changed stamp");
        check(ref.getReference() == a1, "attemptStamp left reference untouched");
        // 引用失配（identity）→ 返回 false 且戳不变
        check(!ref.attemptStamp(a2, 9), "attemptStamp with non-matching reference returns false");
        check(ref.getStamp() == 8, "failed attemptStamp leaves stamp unchanged");
        // set 后无条件更新两者
        ref.set("next", 10);
        check("next".equals(ref.getReference()) && ref.getStamp() == 10, "set updates reference and stamp");
    }

    static void testWeakCas() {
        AtomicStampedReference<Integer> ref = new AtomicStampedReference<>(1, 1);
        check(!ref.weakCompareAndSet(2, 3, 1, 2), "weakCAS mismatch returns false");
        int spins = 0;
        while (!ref.weakCompareAndSet(1, 4, 1, 2) && spins < 100_000) spins++;
        check(ref.getReference() == 4 && ref.getStamp() == 2, "weakCAS success eventually applies");
        check(!ref.weakCompareAndSet(4, 5, 1, 3), "weakCAS with stale stamp returns false");
        check(ref.getReference() == 4 && ref.getStamp() == 2, "stale weakCAS leaves state unchanged");
    }

    public static void main(String[] args) {
        testAtomicStampedReference();
        testCasFailureMatrix();
        testIdentitySemantics();
        testAttemptStamp();
        testWeakCas();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
