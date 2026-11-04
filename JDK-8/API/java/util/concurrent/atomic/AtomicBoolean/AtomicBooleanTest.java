/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicBoolean API conformance: get/set, getAndSet,
 *          and compareAndSet success/failure.
 * @run main AtomicBooleanTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanTest {
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

    static void testAtomicBoolean() {
        AtomicBoolean b = new AtomicBoolean();
        check(!b.get(), "initial false");
        b.set(true);
        check(b.get(), "set/get");
        check(b.getAndSet(false) && !b.get(), "getAndSet");
        check(b.compareAndSet(false, true) && b.get(), "compareAndSet success");
        check(!b.compareAndSet(false, true) && b.get(), "compareAndSet failure");
    }

    // ---- 契约深化：构造器/CAS 全分支/lazySet/weakCAS/toString/序列化/缺席方法 ----
    static void testConstructors() {
        check(!new AtomicBoolean().get(), "default constructor initial false");
        check(new AtomicBoolean(true).get(), "constructor(true)");
        check(!new AtomicBoolean(false).get(), "constructor(false)");
    }

    static void testCasMatrix() {
        // false -> true 成功分支：返回 true 且 get == update
        AtomicBoolean b = new AtomicBoolean(false);
        check(b.compareAndSet(false, true), "CAS false->true returns true");
        check(b.get(), "CAS success leaves value == update");
        // true -> true 失败分支（期望 false 与现值 true 不符）：返回 false 且值不变
        check(!b.compareAndSet(false, true), "CAS stale expect returns false");
        check(b.get(), "CAS failure leaves value unchanged");
        // true -> false 成功
        check(b.compareAndSet(true, false) && !b.get(), "CAS true->false success");
        // 同值新参（true->true）成功且不改变语义
        b.set(true);
        check(b.compareAndSet(true, true) && b.get(), "CAS same-value success");
        // getAndSet 返回旧布尔（两个方向）
        check(b.getAndSet(false) && !b.get(), "getAndSet true->false returns old true");
        check(!b.getAndSet(true) && b.get(), "getAndSet false->true returns old false");
    }

    static void testLazySetAndWeak() {
        AtomicBoolean b = new AtomicBoolean(false);
        b.lazySet(true);
        check(b.get(), "lazySet then get is immediately visible in same thread");
        // weakCompareAndSet（JDK-8 存在；JDK-9+ 废弃）：失败分支确定
        check(!b.weakCompareAndSet(false, true) && b.get(), "weakCAS failure returns false, unchanged");
        // 成功分支可能虚假失败，有界重试
        int spins = 0;
        while (!b.weakCompareAndSet(true, false) && spins < 100_000) spins++;
        check(!b.get(), "weakCAS success eventually applies uncontended");
    }

    static void testToStringAndSerializable() throws Exception {
        check(new AtomicBoolean(true).toString().equals("true"), "toString true");
        check(new AtomicBoolean(false).toString().equals("false"), "toString false");

        AtomicBoolean b = new AtomicBoolean(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(b);
        }
        AtomicBoolean copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (AtomicBoolean) ois.readObject();
        }
        check(copy.get(), "serialization round-trip preserves value");
        check(copy.compareAndSet(true, false) && !copy.get(), "deserialized instance remains operational");
    }

    // JDK-9+ 新增访问模式族在 JDK-8 中缺席（反射断言记录差异）
    static void testAbsentJdk9Methods() throws Exception {
        Class<?> c = AtomicBoolean.class;
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
        // JDK-8 的 weakCompareAndSet 尚未标 @Deprecated（JDK-9 起废弃）
        java.lang.reflect.Method w = c.getMethod("weakCompareAndSet", boolean.class, boolean.class);
        check(!w.isAnnotationPresent(Deprecated.class), "weakCompareAndSet not yet deprecated in JDK-8");
    }

    public static void main(String[] args) throws Exception {
        testAtomicBoolean();
        testConstructors();
        testCasMatrix();
        testLazySetAndWeak();
        testToStringAndSerializable();
        testAbsentJdk9Methods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
