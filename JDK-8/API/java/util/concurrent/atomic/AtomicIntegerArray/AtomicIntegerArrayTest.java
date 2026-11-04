/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicIntegerArray API conformance: length, get/set,
 *          getAndIncrement/incrementAndGet, addAndGet, and compareAndSet.
 * @run main AtomicIntegerArrayTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayTest {
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
        AtomicIntegerArray a = new AtomicIntegerArray(3);
        check(a.length() == 3, "length");
        check(a.get(0) == 0, "initial 0");
        a.set(0, 5);
        check(a.get(0) == 5, "set/get");
        check(a.getAndIncrement(0) == 5 && a.get(0) == 6, "getAndIncrement");
        check(a.incrementAndGet(1) == 1, "incrementAndGet");
        check(a.addAndGet(0, 10) == 16, "addAndGet");
        check(a.compareAndSet(0, 16, 99) && a.get(0) == 99, "compareAndSet success");
        check(!a.compareAndSet(0, 0, 1) && a.get(0) == 99, "compareAndSet failure");
    }

    // ---- 契约深化：构造器拷贝 / 索引独立性 / 越界矩阵 / lazySet / toString / 序列化 / J9+ 缺席 ----
    static void testConstructors() {
        AtomicIntegerArray a = new AtomicIntegerArray(0);
        check(a.length() == 0, "zero length allowed");
        int[] seed = {1, 2, 3};
        AtomicIntegerArray b = new AtomicIntegerArray(seed);
        check(b.length() == 3, "length from int[] constructor");
        check(b.get(0) == 1 && b.get(1) == 2 && b.get(2) == 3, "int[] constructor copies values");
        seed[0] = 99;
        check(b.get(0) == 1, "int[] constructor does not alias source array");
        assertThrows(NegativeArraySizeException.class, () -> new AtomicIntegerArray(-1),
                "negative length constructor throws NegativeArraySizeException");
    }

    static void testIndexIndependence() {
        AtomicIntegerArray a = new AtomicIntegerArray(2);
        // 索引 0 与 1 互不干扰
        a.set(0, 10);
        a.set(1, 20);
        check(a.getAndAdd(0, 1) == 10 && a.get(0) == 11, "getAndAdd index 0");
        check(a.get(1) == 20, "index 1 untouched by index 0 ops");
        check(a.compareAndSet(1, 20, 21), "CAS index 1");
        check(a.get(0) == 11, "index 0 untouched by index 1 CAS");
        // getAndSet / decrement / update 按索引
        check(a.getAndSet(0, 100) == 11 && a.get(0) == 100, "getAndSet per index");
        check(a.getAndDecrement(1) == 21 && a.get(1) == 20, "getAndDecrement per index");
        check(a.decrementAndGet(1) == 19, "decrementAndGet per index");
        check(a.getAndUpdate(0, x -> x - 1) == 100 && a.get(0) == 99, "getAndUpdate per index");
        check(a.updateAndGet(0, x -> x + 1) == 100, "updateAndGet per index");
        check(a.getAndAccumulate(0, 5, (x, y) -> x + y) == 100 && a.get(0) == 105, "getAndAccumulate per index");
        check(a.accumulateAndGet(0, 2, (x, y) -> x * y) == 210, "accumulateAndGet per index");
        a.lazySet(1, 7);
        check(a.get(1) == 7, "lazySet per index");
    }

    static void testIndexBounds() {
        final AtomicIntegerArray a = new AtomicIntegerArray(2);
        // JDK-8 实测：get/set/CAS 越界抛 java.lang.IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1), "get(-1)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(2), "get(length)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(-1, 1), "set(-1, v)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(2, 1), "set(length, v)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.compareAndSet(2, 0, 1), "compareAndSet(length, ..)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.getAndAdd(3, 1), "getAndAdd(3, ..)");
        assertThrows(IndexOutOfBoundsException.class, () -> a.lazySet(5, 1), "lazySet(5, ..)");
        check(a.get(0) == 0 && a.get(1) == 0, "failed bound-checked ops leave values untouched");
    }

    static void testToStringAndSerialization() throws Exception {
        AtomicIntegerArray a = new AtomicIntegerArray(new int[]{1, 2, 3});
        String s = a.toString();
        check(s.contains("1") && s.contains("2") && s.contains("3"), "toString contains values");
        check(new AtomicIntegerArray(0).toString().equals("[]"), "toString of empty array");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        AtomicIntegerArray copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (AtomicIntegerArray) ois.readObject();
        }
        check(copy.length() == 3, "serialization preserves length");
        check(copy.get(0) == 1 && copy.get(1) == 2 && copy.get(2) == 3, "serialization preserves elements");
        copy.set(0, 42);
        check(a.get(0) == 1, "deserialized copy is independent of original");
        check(copy.compareAndSet(1, 2, 22), "deserialized instance remains operational");
    }

    // JDK-9+ 索引化访问模式族在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = AtomicIntegerArray.class;
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
