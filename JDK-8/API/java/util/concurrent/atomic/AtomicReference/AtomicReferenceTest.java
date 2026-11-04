/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReference API conformance: get/set, getAndSet,
 *          and compareAndSet success/failure.
 * @run main AtomicReferenceTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAtomicReference() {
        AtomicReference<String> r = new AtomicReference<>();
        check(r.get() == null, "initial null");
        r.set("a");
        check("a".equals(r.get()), "set/get");
        check("a".equals(r.getAndSet("b")) && "b".equals(r.get()), "getAndSet");
        check(r.compareAndSet("b", "c") && "c".equals(r.get()), "compareAndSet success");
        check(!r.compareAndSet("x", "y") && "c".equals(r.get()), "compareAndSet failure");
    }

    // ---- 契约深化：identity 语义 / update-accumulate / lazySet / null / 序列化 / J9+ 缺席 ----
    static void testIdentitySemantics() {
        // CAS 按 ==（引用同一性）匹配：两个 equals() 相等的不同对象 CAS 失败
        String a1 = new String("a");
        String a2 = new String("a");
        check(a1.equals(a2) && a1 != a2, "precondition: equals but not same reference");
        AtomicReference<String> r = new AtomicReference<>(a1);
        check(!r.compareAndSet(a2, "b"), "CAS with equals-but-not-same expected fails");
        check(r.get() == a1, "failed identity CAS leaves same reference in place");
        check(r.compareAndSet(a1, "b"), "CAS with identical (==) expected succeeds");
        check("b".equals(r.get()), "successful identity CAS updates value");
        // getAndSet / getAndUpdate 同样基于引用
        r.set(a2);
        check(r.get() == a2, "set stores exact reference");
        check(r.getAndSet(a1) == a2, "getAndSet returns exact old reference");
        // null 期望值也参与 identity 匹配
        AtomicReference<String> n = new AtomicReference<>(null);
        check(n.compareAndSet(null, "x"), "CAS(null, x) on null initial succeeds");
        check(!n.compareAndSet(null, "y"), "CAS(null, y) after set fails");
        n.set(null);
        check(n.compareAndSet("x", "y") == false && n.get() == null, "CAS with non-null expect on null value fails");
    }

    static void testUpdateAndAccumulate() {
        AtomicReference<String> r = new AtomicReference<>("a");
        // 函数收到旧引用，getAndUpdate 返回旧值
        java.util.List<String> seen = new java.util.ArrayList<>();
        String old = r.getAndUpdate(s -> { seen.add(s); return s + "1"; });
        check(seen.size() == 1 && "a".equals(seen.get(0)), "getAndUpdate function receives old value");
        check("a".equals(old) && "a1".equals(r.get()), "getAndUpdate returns old, stores new");
        // updateAndGet 返回新值
        check("a11".equals(r.updateAndGet(s -> s + "1")), "updateAndGet returns new value");
        // accumulateAndGet 组合器收到 (旧引用, x)
        java.util.List<String> accArgs = new java.util.ArrayList<>();
        String res = r.accumulateAndGet("x", (s, t) -> { accArgs.add(s); accArgs.add(t); return s + t; });
        check("a11".equals(accArgs.get(0)) && "x".equals(accArgs.get(1)), "accumulateAndGet combiner receives (old, x)");
        check("a11x".equals(res), "accumulateAndGet returns new value");
        // getAndAccumulate 返回旧值
        check("a11x".equals(r.getAndAccumulate("y", (s, t) -> s + t)), "getAndAccumulate returns old value");
        check("a11xy".equals(r.get()), "getAndAccumulate stores combined");
    }

    static void testLazySetToStringSerialization() throws Exception {
        AtomicReference<String> r = new AtomicReference<>("old");
        r.lazySet("new");
        check("new".equals(r.get()), "lazySet then get immediately visible in same thread");
        check(new AtomicReference<>("v").toString().equals("v"), "toString is value toString");
        check(new AtomicReference<>(null).toString().equals("null"), "toString of null value");

        AtomicReference<String> s = new AtomicReference<>("keep");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(s);
        }
        AtomicReference<String> copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            @SuppressWarnings("unchecked")
            AtomicReference<String> t = (AtomicReference<String>) ois.readObject();
            copy = t;
        }
        check("keep".equals(copy.get()), "serialization round-trip preserves reference value");
        // 反序列化会重建 String 对象：字面量 "keep"（驻留）与流中读出的对象 == 不匹配 → identity CAS 失败
        check(!copy.compareAndSet("keep", "nope"), "identity CAS with interned literal fails on deserialized value");
        check(copy.compareAndSet(copy.get(), "kept"), "CAS with actual stored reference succeeds after round-trip");
        check("kept".equals(copy.get()), "deserialized instance remains operational");
    }

    // JDK-9+ 访问模式族在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = AtomicReference.class;
        String[] absent = {
            "getPlain", "setPlain", "getOpaque", "setOpaque",
            "getAcquire", "setRelease",
            "compareAndExchange", "compareAndExchangeAcquire", "compareAndExchangeRelease",
            "weakCompareAndSetPlain", "weakCompareAndSetVolatile",
            "weakCompareAndSetAcquire", "weakCompareAndSetRelease",
        };
        for (String name : absent) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) {
                if (m.getName().equals(name)) { found = true; break; }
            }
            check(!found, name + " must be absent in JDK-8");
        }
        // weakCompareAndSet 两分支（JDK-8 存在；JDK-9+ 废弃）
        AtomicReference<String> r = new AtomicReference<>("w");
        check(!r.weakCompareAndSet("other", "z"), "weakCAS failure returns false");
        int spins = 0;
        while (!r.weakCompareAndSet("w", "weak") && spins < 100_000) spins++;
        check("weak".equals(r.get()), "weakCAS success eventually applies uncontended");
        java.lang.reflect.Method w = c.getMethod("weakCompareAndSet", Object.class, Object.class);
        check(!w.isAnnotationPresent(Deprecated.class), "weakCompareAndSet not yet deprecated in JDK-8");
    }

    public static void main(String[] args) throws Exception {
        testAtomicReference();
        testIdentitySemantics();
        testUpdateAndAccumulate();
        testLazySetToStringSerialization();
        testAbsentJdk9Methods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
