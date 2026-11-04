/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLongArray API conformance: length, get/set,
 *          getAndIncrement/incrementAndGet, addAndGet, and compareAndSet.
 * @run main AtomicLongArrayTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayTest {
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

    static void testArray() {
        AtomicLongArray a = new AtomicLongArray(3);
        check(a.length() == 3, "length");
        check(a.get(0) == 0L, "initial 0");
        a.set(0, 5L);
        check(a.get(0) == 5L, "set/get");
        check(a.getAndIncrement(0) == 5L && a.get(0) == 6L, "getAndIncrement");
        check(a.incrementAndGet(1) == 1L, "incrementAndGet");
        check(a.addAndGet(0, 10L) == 16L, "addAndGet");
        check(a.compareAndSet(0, 16L, 99L) && a.get(0) == 99L, "compareAndSet success");
        check(!a.compareAndSet(0, 0L, 1L) && a.get(0) == 99L, "compareAndSet failure");
    }

    // ---- 契约深化：构造器拷贝 / 索引独立性 / 越界矩阵 / lazySet / toString / 序列化 / J9+ 缺席 ----
    static void testConstructors() {
        AtomicLongArray a = new AtomicLongArray(0);
        check(a.length() == 0, "zero length allowed");
        long[] seed = {1L, 2L, 3L};
        AtomicLongArray b = new AtomicLongArray(seed);
        check(b.length() == 3, "length from long[] constructor");
        check(b.get(0) == 1L && b.get(1) == 2L && b.get(2) == 3L, "long[] constructor copies values");
        seed[0] = 99L;
        check(b.get(0) == 1L, "long[] constructor does not alias source array");
        assertThrows(NegativeArraySizeException.class, () -> new AtomicLongArray(-1),
                "negative length constructor throws NegativeArraySizeException");
    }

    static void testIndexIndependence() {
        AtomicLongArray a = new AtomicLongArray(2);
        a.set(0, 10L);
        a.set(1, 20L);
        check(a.getAndAdd(0, 1L) == 10L && a.get(0) == 11L, "getAndAdd index 0");
        check(a.get(1) == 20L, "index 1 untouched by index 0 ops");
        check(a.compareAndSet(1, 20L, 21L), "CAS index 1");
        check(a.get(0) == 11L, "index 0 untouched by index 1 CAS");
        check(a.getAndSet(0, 100L) == 11L && a.get(0) == 100L, "getAndSet per index");
        check(a.getAndDecrement(1) == 21L && a.get(1) == 20L, "getAndDecrement per index");
        check(a.decrementAndGet(1) == 19L, "decrementAndGet per index");
        check(a.getAndUpdate(0, x -> x - 1L) == 100L && a.get(0) == 99L, "getAndUpdate per index");
        check(a.updateAndGet(0, x -> x + 1L) == 100L, "updateAndGet per index");
        check(a.getAndAccumulate(0, 5L, (x, y) -> x + y) == 100L && a.get(0) == 105L, "getAndAccumulate per index");
        check(a.accumulateAndGet(0, 2L, (x, y) -> x * y) == 210L, "accumulateAndGet per index");
        a.lazySet(1, 7L);
        check(a.get(1) == 7L, "lazySet per index");
        // 64 位边界
        a.set(0, Long.MAX_VALUE);
        check(a.incrementAndGet(0) == Long.MIN_VALUE, "per-index increment wraps MAX to MIN");
    }

    static void testIndexBounds() {
        final AtomicLongArray a = new AtomicLongArray(2);
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1), "get(-1)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(2), "get(length)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(-1, 1L), "set(-1, v)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(2, 1L), "set(length, v)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.compareAndSet(2, 0L, 1L), "compareAndSet(length, ..)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.getAndAdd(3, 1L), "getAndAdd(3, ..)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.lazySet(5, 1L), "lazySet(5, ..)");
        check(a.get(0) == 0L && a.get(1) == 0L, "failed bound-checked ops leave values untouched");
    }

    static void testToStringAndSerialization() throws Exception {
        AtomicLongArray a = new AtomicLongArray(new long[]{1L, 2L, 3L});
        String s = a.toString();
        check(s.contains("1") && s.contains("2") && s.contains("3"), "toString contains values");
        check(new AtomicLongArray(0).toString().equals("[]"), "toString of empty array");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        AtomicLongArray copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (AtomicLongArray) ois.readObject();
        }
        check(copy.length() == 3, "serialization preserves length");
        check(copy.get(0) == 1L && copy.get(1) == 2L && copy.get(2) == 3L, "serialization preserves elements");
        copy.set(0, 42L);
        check(a.get(0) == 1L, "deserialized copy is independent of original");
        check(copy.compareAndSet(1, 2L, 22L), "deserialized instance remains operational");
    }

    // JDK-9+ 索引化访问模式族在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = AtomicLongArray.class;
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
        testArray();
        testConstructors();
        testIndexIndependence();
        testIndexBounds();
        testToStringAndSerialization();
        testAbsentJdk9Methods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
