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

    // ---- 契约深化：四参失配矩阵 / identity 语义 / attemptMark 两分支 / weakCAS ----
    static void testCasFailureMatrix() {
        // 全匹配 → true
        AtomicMarkableReference<String> ref = new AtomicMarkableReference<>("r", true);
        check(ref.compareAndSet("r", "s", true, false), "all-match CAS succeeds");
        check("s".equals(ref.getReference()) && !ref.isMarked(), "all-match CAS applies ref and mark");
        // ref 失配 → false 且不变
        check(!ref.compareAndSet("r", "t", false, true), "ref-mismatch CAS returns false");
        check("s".equals(ref.getReference()) && !ref.isMarked(), "ref-mismatch leaves state unchanged");
        // mark 失配 → false 且不变
        check(!ref.compareAndSet("s", "t", true, true), "mark-mismatch CAS returns false");
        check("s".equals(ref.getReference()) && !ref.isMarked(), "mark-mismatch leaves state unchanged");
        // ref 与 mark 都失配 → false 且不变
        check(!ref.compareAndSet("r", "t", true, true), "both-mismatch CAS returns false");
        check("s".equals(ref.getReference()) && !ref.isMarked(), "both-mismatch leaves state unchanged");
        // 仅改 mark 不动引用
        check(ref.compareAndSet("s", "s", false, true), "same-ref mark-only CAS succeeds");
        check("s".equals(ref.getReference()) && ref.isMarked(), "mark-only CAS changes only mark");
        // expectedRef 为 null 的分支
        AtomicMarkableReference<String> n = new AtomicMarkableReference<>(null, false);
        check(n.compareAndSet(null, "v", false, true), "CAS(null, v, false, true) on null initial succeeds");
        check(!n.compareAndSet(null, "w", true, false), "CAS(null, w) after set fails");
    }

    static void testIdentitySemantics() {
        String a1 = new String("a");
        String a2 = new String("a");
        check(a1.equals(a2) && a1 != a2, "precondition: equals but not same reference");
        AtomicMarkableReference<String> ref = new AtomicMarkableReference<>(a1, false);
        check(!ref.compareAndSet(a2, "b", false, true), "identity CAS with equals-but-not-same ref fails");
        check(ref.getReference() == a1 && !ref.isMarked(), "failed identity CAS leaves state unchanged");
        check(ref.compareAndSet(a1, "b", false, true), "identity CAS with == ref succeeds");
    }

    static void testAttemptMark() {
        String a1 = new String("a");
        String a2 = new String("a");
        AtomicMarkableReference<String> ref = new AtomicMarkableReference<>(a1, false);
        // 引用匹配 → 只改 mark，返回 true
        check(ref.attemptMark(a1, true), "attemptMark with matching reference returns true");
        check(ref.isMarked(), "attemptMark changed mark");
        check(ref.getReference() == a1, "attemptMark left reference untouched");
        // 引用失配（identity）→ 返回 false 且 mark 不变
        check(!ref.attemptMark(a2, false), "attemptMark with non-matching reference returns false");
        check(ref.isMarked(), "failed attemptMark leaves mark unchanged");
        // set 无条件更新两者
        ref.set("next", false);
        check("next".equals(ref.getReference()) && !ref.isMarked(), "set updates reference and mark");
        boolean[] mark = new boolean[1];
        check("next".equals(ref.get(mark)) && !mark[0], "get(holder) fills mark holder");
    }

    static void testWeakCas() {
        AtomicMarkableReference<String> ref = new AtomicMarkableReference<>("w", false);
        check(!ref.weakCompareAndSet("x", "y", false, true), "weakCAS mismatch returns false");
        int spins = 0;
        while (!ref.weakCompareAndSet("w", "v", false, true) && spins < 100_000) spins++;
        check("v".equals(ref.getReference()) && ref.isMarked(), "weakCAS success eventually applies");
        check(!ref.weakCompareAndSet("v", "u", false, false), "weakCAS with stale mark returns false");
        check("v".equals(ref.getReference()) && ref.isMarked(), "stale weakCAS leaves state unchanged");
    }

    public static void main(String[] args) {
        testAtomicMarkableReference();
        testCasFailureMatrix();
        testIdentitySemantics();
        testAttemptMark();
        testWeakCas();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
