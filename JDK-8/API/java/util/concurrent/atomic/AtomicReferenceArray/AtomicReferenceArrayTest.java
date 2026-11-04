/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicReferenceArray API conformance: get/set and
 *          compareAndSet.
 * @run main AtomicReferenceArrayTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static void testAtomicReferenceArray() {
        AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(3);
        check(arr.length() == 3, "length");
        arr.set(0, "a");
        check("a".equals(arr.get(0)), "get");
        check(arr.compareAndSet(0, "a", "b"), "compareAndSet");
        check("b".equals(arr.get(0)), "after CAS");
        check(!arr.compareAndSet(0, "a", "c"), "CAS with stale expected fails");
    }

    // ---- 契约深化：identity 语义 / 越界矩阵 / update 族 / null 元素 / 序列化 / J9+ 缺席 ----
    static void testIdentitySemantics() {
        String a1 = new String("a");
        String a2 = new String("a");
        check(a1.equals(a2) && a1 != a2, "precondition: equals but not same reference");
        AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(2);
        arr.set(0, a1);
        check(!arr.compareAndSet(0, a2, "z"), "indexed CAS with equals-but-not-same expected fails");
        check(arr.get(0) == a1, "failed identity CAS leaves same reference");
        check(arr.compareAndSet(0, a1, "b"), "indexed CAS with identical (==) expected succeeds");
        check(arr.getAndSet(0, a1) == (Object) "b", "indexed getAndSet returns exact old reference");
        // null 元素契约：初始 null，可 CAS(null → v)，可 set null
        check(arr.get(1) == null, "initial element null");
        check(arr.compareAndSet(1, null, "n"), "indexed CAS(null, v) on null element succeeds");
        check(!arr.compareAndSet(1, null, "m"), "indexed CAS(null, m) after set fails");
        arr.set(1, null);
        check(arr.get(1) == null, "set(null) stores null");
    }

    static void testConstructorsAndBounds() {
        String[] seed = {"a", "b"};
        AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(seed);
        check(arr.length() == 2, "length from E[] constructor");
        check("a".equals(arr.get(0)) && "b".equals(arr.get(1)), "E[] constructor copies values");
        seed[0] = "changed";
        check("a".equals(arr.get(0)), "E[] constructor does not alias source array");
        check(new AtomicReferenceArray<>(0).length() == 0, "zero length allowed");
        assertThrows(NegativeArraySizeException.class, () -> new AtomicReferenceArray<>(-1),
                "negative length constructor throws NegativeArraySizeException");

        final AtomicReferenceArray<String> a = new AtomicReferenceArray<>(2);
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1), "get(-1)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(2), "get(length)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(-1, "x"), "set(-1, v)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(2, "x"), "set(length, v)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.compareAndSet(2, "a", "b"), "compareAndSet(length, ..)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.getAndSet(5, "x"), "getAndSet(5, ..)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.lazySet(7, "x"), "lazySet(7, ..)");
        check(a.get(0) == null && a.get(1) == null, "failed bound-checked ops leave elements untouched");
    }

    static void testUpdateFamily() {
        AtomicReferenceArray<String> a = new AtomicReferenceArray<>(2);
        a.set(0, "a");
        java.util.List<String> seen = new java.util.ArrayList<>();
        check("a".equals(a.getAndUpdate(0, s -> { seen.add(s); return s + "1"; })),
                "getAndUpdate(index) returns old value");
        check(seen.size() == 1 && "a".equals(seen.get(0)), "getAndUpdate function receives old element");
        check("a1".equals(a.get(0)), "getAndUpdate stores new element");
        check("a11".equals(a.updateAndGet(0, s -> s + "1")), "updateAndGet(index) returns new value");
        check("a11".equals(a.getAndAccumulate(0, "x", (s, t) -> s + t)) && "a11x".equals(a.get(0)),
                "getAndAccumulate(index) returns old, stores combined");
        check("a11xy".equals(a.accumulateAndGet(0, "y", (s, t) -> s + t)), "accumulateAndGet(index) returns new");
        // 索引 0 与 1 互不干扰
        a.set(1, "q");
        a.updateAndGet(0, s -> s + "!");
        check("q".equals(a.get(1)), "index 1 untouched by index 0 ops");
        check(a.toString().contains("a11xy"), "toString contains element");
        check(new AtomicReferenceArray<>(0).toString().equals("[]"), "toString of empty array");
    }

    static void testSerialization() throws Exception {
        AtomicReferenceArray<String> a = new AtomicReferenceArray<>(new String[]{"x", "y"});
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        AtomicReferenceArray<String> copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            @SuppressWarnings("unchecked")
            AtomicReferenceArray<String> t = (AtomicReferenceArray<String>) ois.readObject();
            copy = t;
        }
        check(copy.length() == 2, "serialization preserves length");
        check("x".equals(copy.get(0)) && "y".equals(copy.get(1)), "serialization preserves elements");
        copy.set(0, "z");
        check("x".equals(a.get(0)), "deserialized copy is independent of original");
        // 反序列化会重建 String 对象：用实际存储引用做 identity CAS
        check(copy.compareAndSet(1, copy.get(1), "w"), "CAS with actual stored reference succeeds after round-trip");
        check("w".equals(copy.get(1)), "deserialized instance remains operational");
    }

    // JDK-9+ 索引化访问模式族在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = AtomicReferenceArray.class;
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
    }

    public static void main(String[] args) throws Exception {
        testAtomicReferenceArray();
        testIdentitySemantics();
        testConstructorsAndBounds();
        testUpdateFamily();
        testSerialization();
        testAbsentJdk9Methods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
