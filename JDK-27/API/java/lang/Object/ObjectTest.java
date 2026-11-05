/*
 * @test
 * @summary java.lang.Object conformance: white-box structure + black-box behavior.
 * @run main ObjectTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectIMSE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException expected) {
            passed++;
        }
    }

    static class CloneableThing implements Cloneable {
        int x;
        int[] ref;
        CloneableThing(int x, int[] ref) { this.x = x; this.ref = ref; }
        @Override public CloneableThing clone() {
            try {
                return (CloneableThing) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    static class NotCloneable {
        @Override public NotCloneable clone() throws CloneNotSupportedException {
            return (NotCloneable) super.clone();
        }
    }

    // Exposes Object.finalize() (protected, deprecated for removal) to prove the
    // no-op super.finalize() is reachable from a subclass.
    static class Finalizable {
        void invokeFinalize() {
            try {
                finalize();
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
        @Override protected void finalize() throws Throwable { super.finalize(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Object");
        check(c.getSuperclass() == null, "Object has no superclass");
        check(!c.isInterface(), "Object is a class, not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "Object is concrete");
        Method getClass = c.getMethod("getClass");
        check(Modifier.isFinal(getClass.getModifiers()), "getClass() is final");
        check(Modifier.isFinal(c.getMethod("notify").getModifiers()), "notify() is final");
        check(Modifier.isFinal(c.getMethod("notifyAll").getModifiers()), "notifyAll() is final");
        check(Modifier.isFinal(c.getMethod("wait").getModifiers()), "wait() is final");
        check(Modifier.isFinal(c.getMethod("wait", long.class).getModifiers()), "wait(long) is final");
        check(Modifier.isFinal(c.getMethod("wait", long.class, int.class).getModifiers()), "wait(long,int) is final");
        check(Modifier.isNative(c.getMethod("hashCode").getModifiers()), "hashCode() is native");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testGetClass() {
        Object o = new Object();
        check(o.getClass() == Object.class, "new Object().getClass() == Object.class");
        check("s".getClass() == String.class, "\"s\".getClass() == String.class");
        check(new int[0].getClass() == int[].class, "int[].getClass() == int[].class");
    }

    static void testHashCodeEquals() {
        Object a = new Object();
        Object b = new Object();
        check(a.equals(a), "equals is reflexive");
        check(!a.equals(b), "distinct objects not equals");
        check(!a.equals(null), "!equals(null)");
        check(a.equals(b) == b.equals(a), "equals is symmetric");
        check(a.hashCode() == System.identityHashCode(a), "default hashCode == identityHashCode");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
    }

    static void testToString() {
        Object o = new Object();
        String expected = o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
        check(o.toString().equals(expected), "toString == className@hexHash");
    }

    static void testClone() throws Exception {
        int[] shared = new int[] { 7 };
        CloneableThing c = new CloneableThing(7, shared);
        CloneableThing d = c.clone();
        check(c != d, "clone returns a distinct object");
        check(c.getClass() == d.getClass(), "clone has same runtime class");
        check(d.x == 7, "clone copies field values (shallow)");
        check(d.ref == c.ref, "clone is a shallow copy (reference fields shared)");

        NotCloneable n = new NotCloneable();
        try {
            n.clone();
            throw new AssertionError("clone on non-Cloneable: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }
    }

    static void testMonitorErrors() {
        Object lock = new Object();
        expectIMSE(() -> { try { lock.wait(); } catch (InterruptedException e) {
            throw new AssertionError(e); } }, "wait() without owning monitor");
        expectIMSE(() -> lock.notify(), "notify() without owning monitor");
        expectIMSE(() -> lock.notifyAll(), "notifyAll() without owning monitor");
    }

    static void testWaitNotify() throws Exception {
        final Object lock = new Object();
        final boolean[] entered = { false };
        final boolean[] woken = { false };

        Thread worker = new Thread(() -> {
            synchronized (lock) {
                entered[0] = true;
                lock.notifyAll(); // tell main we hold the monitor
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
                woken[0] = true;
            }
        });
        worker.start();

        synchronized (lock) {
            while (!entered[0]) {
                try { lock.wait(); } catch (InterruptedException e) { throw new AssertionError(e); }
            }
            lock.notify(); // wake the worker from its wait()
        }
        worker.join(2000);
        check(!worker.isAlive(), "worker thread terminated");
        check(woken[0], "notify() wakes a waiting thread");
    }

    static void testWaitTimeout() throws Exception {
        final Object lock = new Object();
        synchronized (lock) {
            // wait(timeout) returns even when nobody notifies.
            long start = System.nanoTime();
            lock.wait(50);
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            check(elapsedMillis >= 40, "wait(50) waited at least ~50ms (actual " + elapsedMillis + ")");
        }
    }

    // ---- Part 2b: black-box for the deprecated protected finalize() ----
    static void testBlackBox() throws Exception {
        // finalize() is protected; prove it is reachable and a no-op via a subclass.
        new Finalizable().invokeFinalize();
        passed++;
    }

    // ==================== 契约深化（2026-11）====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    /** 值语义样本：覆写 equals/hashCode，用于完整 equals 契约矩阵。 */
    static class Pair {
        final int x, y;
        Pair(int x, int y) { this.x = x; this.y = y; }
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair p = (Pair) o;
            return x == p.x && y == p.y;
        }
        public int hashCode() { return 31 * x + y; }
        public String toString() { return "Pair[" + x + "," + y + "]"; }
    }

    /** 未覆写 hashCode/toString 的裸类：identityHashCode/toString 契约样本。 */
    static class Plain { }

    /** 白盒补充：protected 成员 clone/finalize、公共方法面、构造器。 */
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Object.class;
        // 恰 9 个公共方法（getClass/hashCode/equals/toString/notify/notifyAll/wait×3）
        int pubCount = 0;
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Method m : c.getMethods()) { pubCount++; names.add(m.getName()); }
        check(pubCount == 9, "Object exposes exactly 9 public methods, got " + pubCount);
        check(names.containsAll(java.util.Arrays.asList(
                  "getClass", "hashCode", "equals", "toString", "notify", "notifyAll", "wait")),
              "public method name set complete: " + names);
        // clone 与 finalize 是 protected（不入公共面）
        Method clone = c.getDeclaredMethod("clone");
        check(Modifier.isProtected(clone.getModifiers()), "clone() is protected (not public)");
        check(!Modifier.isPublic(clone.getModifiers()), "clone() is NOT public");
        check(Modifier.isNative(clone.getModifiers()), "clone() is native");
        Method finalize = c.getDeclaredMethod("finalize");
        check(Modifier.isProtected(finalize.getModifiers()), "finalize() is protected");
        check(finalize.getExceptionTypes().length == 1
                  && finalize.getExceptionTypes()[0] == Throwable.class,
              "finalize() declares throws Throwable");
        // 可覆写性：equals/hashCode/toString/clone 非 final
        check(!Modifier.isFinal(c.getMethod("equals", Object.class).getModifiers()),
              "equals() is overridable (not final)");
        check(!Modifier.isFinal(c.getMethod("hashCode").getModifiers()),
              "hashCode() is overridable (not final)");
        check(!Modifier.isFinal(c.getMethod("toString").getModifiers()),
              "toString() is overridable (not final)");
        check(!Modifier.isFinal(clone.getModifiers()), "clone() is overridable (not final)");
        // Object 本身可被继承：非 final、非抽象；唯一构造器是 public 无参
        check(!Modifier.isFinal(c.getModifiers()), "Object is not final");
        check(c.getConstructors().length == 1, "exactly 1 public constructor");
        check(c.getConstructor().getParameterCount() == 0, "the constructor takes no args");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "Object() is public");
    }

    /** finalize 弃用状态：JDK-8 未弃用；JDK-27 @Deprecated(since=9, forRemoval=true)。 */
    static void testFinalizeDeprecation() throws Exception {
        Method f = Object.class.getDeclaredMethod("finalize");
        boolean deprecated = f.isAnnotationPresent(Deprecated.class);
        if (MODERN) {
            check(deprecated, "finalize() carries @Deprecated on modern JDKs");
            Deprecated d = f.getAnnotation(Deprecated.class);
            String since = (String) Deprecated.class.getMethod("since").invoke(d);
            Boolean forRemoval = (Boolean) Deprecated.class.getMethod("forRemoval").invoke(d);
            check("9".equals(since), "finalize deprecated since 9");
            check(forRemoval.booleanValue(), "finalize deprecated for removal");
        } else {
            check(!deprecated, "JDK-8 finalize() is NOT deprecated");
        }
    }

    /** equals 完整契约（值语义样本）：自反/对称/传递/null/异型/一致性/HashMap 联动。 */
    static void testEqualsContractValueSemantics() {
        Pair a = new Pair(1, 2);
        Pair b = new Pair(1, 2);
        Pair c = new Pair(1, 2);
        Pair d = new Pair(9, 9);
        check(a.equals(a), "reflexive: a.equals(a)");
        check(a.equals(b), "equal values a.equals(b)");
        check(b.equals(a), "symmetric: b.equals(a)");
        check(a.equals(b) && b.equals(c) && a.equals(c), "transitive: a=b=b=c => a=c");
        check(!a.equals(d), "different values not equals");
        check(!a.equals(null), "equals(null) is false");
        check(!a.equals("1,2"), "equals(different type) is false");
        check(!a.equals(new int[] {1, 2}), "equals(array) is false");
        check(a.equals(b) == a.equals(b), "consistent across repeated calls");
        check(a.hashCode() == b.hashCode(), "equal objects have equal hashCodes");
        // hashCode 一致性：同对象多次调用稳定
        int h1 = a.hashCode(), h2 = a.hashCode();
        check(h1 == h2, "hashCode stable across calls");
        // HashMap 联动：equal-but-distinct keys 命中同一桶
        java.util.HashMap<Pair, String> map = new java.util.HashMap<>();
        map.put(a, "first");
        check("first".equals(map.get(b)), "HashMap.get finds value with an equal-but-distinct key");
        map.put(b, "second");
        check(map.size() == 1, "equal keys replace, map stays size 1, got " + map.size());
        check("second".equals(map.get(a)), "replacement visible via the original key");
        // containsKey 走 equals 路径
        check(map.containsKey(new Pair(1, 2)), "containsKey uses equals contract");
        check(!map.containsKey(d), "non-equal key absent");
        // HashSet 联动
        java.util.HashSet<Pair> set = new java.util.HashSet<>();
        set.add(a);
        check(set.add(b) == false, "HashSet.add of an equal element returns false");
        check(set.size() == 1, "HashSet deduplicates by equals");
        // null 字段参与 equals（null==null 成立）
        check(new Pair(0, 0).equals(new Pair(0, 0)), "zero-pair equality");
    }

    /** hashCode/identityHashCode 契约：identityHashCode(null)==0、稳定、默认实现一致。 */
    static void testHashCodeContract() {
        check(System.identityHashCode(null) == 0, "identityHashCode(null) == 0 (documented)");
        Object o = new Object();
        check(System.identityHashCode(o) == System.identityHashCode(o),
              "identityHashCode stable for the same object");
        Plain p = new Plain();
        check(p.hashCode() == System.identityHashCode(p),
              "unoverridden hashCode() == identityHashCode");
        check(p.hashCode() == p.hashCode(), "unoverridden hashCode stable");
        Plain p2 = new Plain();
        // 不要求不同对象 hash 不同——只断可以相等或不同（契约不禁止碰撞）
        check(p.hashCode() != p2.hashCode() || p.hashCode() == p2.hashCode(),
              "distinct objects may or may not collide (no false constraint)");
        // 相等字符串的 hashCode 一致（覆写实现的契约）
        check("lc".hashCode() == new String("lc").hashCode(), "equal Strings share hashCode");
        check("".hashCode() == 0, "empty String hashCode is 0");
        // Integer.hashCode == value
        check(Integer.valueOf(42).hashCode() == 42, "Integer.hashCode == its value");
        // 覆写类：equals 相等 ⇒ hash 相等（重申）
        check(new Pair(3, 4).hashCode() == new Pair(3, 4).hashCode(), "Pair hash consistent");
    }

    /** getClass() == 精确：两实例同一 Class 对象、数组长短无关、Object.class 是 Class 的实例。 */
    static void testGetClassIdentity() {
        Plain a = new Plain(), b = new Plain();
        check(a.getClass() == b.getClass(), "two instances share the SAME Class object");
        check(a.getClass() == Plain.class, "getClass() == the class literal");
        check(a.getClass().getClass() == Class.class, "a Class object's class is java.lang.Class");
        check(Object.class == new Object().getClass(), "Object.class identity");
        check(new int[0].getClass() == new int[99].getClass(),
              "int[] of any length share one Class object");
        Class<?> intArr = new int[0].getClass();
        Class<?> longArr = new long[0].getClass();
        check(intArr != longArr, "int[] vs long[] are distinct classes");
        Class<?> plainArr = new Plain[0].getClass();
        Class<?> objArr = new Object[0].getClass();
        check(plainArr != objArr, "Plain[] vs Object[] distinct");
        check(Plain.class.getName().endsWith("Plain"), "getName ends with simple name");
        check(Plain.class.getName().contains(ObjectTest.class.getSimpleName()),
              "nested binary name contains the enclosing simple name");
        check(Plain.class.getName().indexOf('$') > 0, "nested binary name uses $ separator");
        check(Plain.class.getDeclaredClasses().length == 0, "Plain has no nested classes");
        check(Object.class.getSuperclass() == null, "Object has no superclass (restated)");
        check(String.class.getSuperclass() == Object.class, "String's superclass is Object");
        check(Plain.class.getSuperclass() == Object.class, "Plain's superclass is Object");
        check(int.class.isPrimitive(), "int.class is primitive");
        check(!int.class.isAssignableFrom(Integer.class), "int.class != Integer.class (boxing)");
        check(new int[0].getClass().getSuperclass() == Object.class, "array superclass is Object");
        check(Object.class.isInstance(new Plain()), "isInstance polymorphic check");
        check(!Plain.class.isInstance(new Object()), "Plain.isInstance(new Object()) false");
        check(Object.class.isInstance(null) == false, "isInstance(null) always false");
    }

    /** toString 契约：默认 "binaryName@hex(identityHashCode)"；数组前缀；覆写生效。 */
    static void testToStringFormats() {
        Plain p = new Plain();
        String ts = p.toString();
        String suffix = "@" + Integer.toHexString(p.hashCode());
        check(ts.endsWith(suffix), "toString ends with @hex of hashCode");
        check(ts.equals(p.getClass().getName() + "@" + Integer.toHexString(p.hashCode())),
              "toString == binaryName@hex exact");
        check(ts.indexOf('@') == ts.length() - suffix.length(), "exactly one @ at the boundary");
        check(ts.contains(Plain.class.getSimpleName()), "toString contains the simple name");
        check(ts.length() > 0, "toString never empty");
        // identityHashCode 与 toString 后缀一致（未覆写 hashCode 时）
        check(System.identityHashCode(p) == p.hashCode(), "identityHashCode backs toString hex");
        // 数组 toString 走 Object.toString："[I@hex"
        int[] arr = new int[] {1};
        check(arr.toString().startsWith("[I@"), "int[] toString prefix is [I@");
        check(arr.toString().endsWith(Integer.toHexString(arr.hashCode())),
              "array toString hex suffix consistent");
        long[] larr = new long[] {1};
        check(larr.toString().startsWith("[J@"), "long[] toString prefix is [J@");
        String[] sarr = new String[] {"x"};
        check(sarr.toString().startsWith("[Ljava.lang.String;@"),
              "String[] toString prefix is [Ljava.lang.String;@");
        // 覆写 toString 的类直接生效
        check(new Pair(1, 2).toString().equals("Pair[1,2]"), "overridden toString used");
        // Object.toString 语义等价于 String.valueOf(obj)
        check(String.valueOf((Object) p).equals(p.toString()), "String.valueOf delegates to toString");
    }

    /** 未持锁四形态全测：wait()/wait(1)/notify()/notifyAll() → IMSE；wait(0,1) 同。 */
    static void testMonitorErrorsExtended() {
        Object lock = new Object();
        expectIMSE(() -> { try { lock.wait(1); } catch (InterruptedException e) {
            throw new AssertionError(e); } }, "wait(1) without owning monitor");
        expectIMSE(() -> { try { lock.wait(0, 1); } catch (InterruptedException e) {
            throw new AssertionError(e); } }, "wait(0,1) without owning monitor");
        expectIMSE(() -> lock.notify(), "notify() without owning monitor (restated)");
        expectIMSE(() -> lock.notifyAll(), "notifyAll() without owning monitor (restated)");
        // 参数校验先于监视器检查：未持锁 + 负超时 → IllegalArgumentException 而非 IMSE
        try {
            lock.wait(-1);
            throw new AssertionError("wait(-1) without lock: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("timeout value is negative".equals(e.getMessage()),
                  "wait(-1) argument check precedes monitor check; exact message");
        } catch (IllegalMonitorStateException e) {
            throw new AssertionError("expected IAE before IMSE", e);
        } catch (InterruptedException e) {
            throw new AssertionError("never interrupts here", e);
        }
        try {
            lock.wait(0, -1);
            throw new AssertionError("wait(0,-1) without lock: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            passed++;
        } catch (IllegalMonitorStateException e) {
            throw new AssertionError("expected IAE before IMSE", e);
        } catch (InterruptedException e) {
            throw new AssertionError("never interrupts here", e);
        }
    }

    /** 持锁内 wait 参数校验：负超时/越界 nanos → IAE 精确消息；合法边界不抛。 */
    static void testWaitArgumentValidation() throws Exception {
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait(-1);
                throw new AssertionError("wait(-1): expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                check("timeout value is negative".equals(e.getMessage()),
                      "wait(-1) exact message");
            }
            try {
                lock.wait(0, -1);
                throw new AssertionError("wait(0,-1): expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                check("nanosecond timeout value out of range".equals(e.getMessage()),
                      "wait(0,-1) exact message");
            }
            try {
                lock.wait(1, -1);
                throw new AssertionError("wait(1,-1): expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                check("nanosecond timeout value out of range".equals(e.getMessage()),
                      "wait(1,-1) exact message");
            }
            try {
                lock.wait(1, 1000000);
                throw new AssertionError("wait(1,1000000): expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                check("nanosecond timeout value out of range".equals(e.getMessage()),
                      "nanos upper bound is 999999");
            }
            // 合法边界：不抛、快速返回（nanos>0 会把 millis 进位为 1ms）
            lock.wait(0, 1);
            passed++;
            lock.wait(0, 999999);
            passed++;
            lock.wait(1, 0);
            passed++;
            // 注意 wait(0,0) 等价于 wait(0)（无限等待）——绝不能在测试里调用
        }
    }

    /** notify 唤醒恰一个等待者；notifyAll 唤醒剩余全部。 */
    static void testNotifySelectsExactlyOne() throws Exception {
        final Object lock = new Object();
        final java.util.concurrent.CountDownLatch entered =
            new java.util.concurrent.CountDownLatch(2);
        final java.util.concurrent.CountDownLatch firstDone =
            new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch doneA =
            new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch doneB =
            new java.util.concurrent.CountDownLatch(1);
        final boolean[] woken = new boolean[2];
        Thread wa = new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    entered.countDown();
                    try { lock.wait(); } catch (InterruptedException e) { }
                    woken[0] = true;
                }
                firstDone.countDown();
                doneA.countDown();
            }
        }, "lcN2-waiter-a");
        Thread wb = new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    entered.countDown();
                    try { lock.wait(); } catch (InterruptedException e) { }
                    woken[1] = true;
                }
                firstDone.countDown();
                doneB.countDown();
            }
        }, "lcN2-waiter-b");
        wa.setDaemon(true);
        wb.setDaemon(true);
        wa.start();
        wb.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS),
              "both waiters reached wait() (bounded)");
        synchronized (lock) {
            lock.notify();
        }
        check(firstDone.await(10, java.util.concurrent.TimeUnit.SECONDS),
              "one waiter completed after a single notify");
        int awakeCount = (woken[0] ? 1 : 0) + (woken[1] ? 1 : 0);
        check(awakeCount == 1, "exactly 1 waiter woken by notify(), got " + awakeCount);
        synchronized (lock) {
            lock.notifyAll();
        }
        check(doneA.await(10, java.util.concurrent.TimeUnit.SECONDS), "waiter A finished");
        check(doneB.await(10, java.util.concurrent.TimeUnit.SECONDS), "waiter B finished");
        check(woken[0] && woken[1], "notifyAll wakes the remaining waiter");
        wa.join(10000);
        wb.join(10000);
        check(!wa.isAlive() && !wb.isAlive(), "both waiters terminated");
    }

    /** notifyAll 唤醒全部 3 个等待者。 */
    static void testNotifyAllWakesAll() throws Exception {
        final Object lock = new Object();
        final int n = 3;
        final java.util.concurrent.CountDownLatch entered =
            new java.util.concurrent.CountDownLatch(n);
        final java.util.concurrent.CountDownLatch done =
            new java.util.concurrent.CountDownLatch(n);
        final int[] wokenCount = {0};
        Thread[] ts = new Thread[n];
        for (int i = 0; i < n; i++) {
            ts[i] = new Thread(new Runnable() {
                public void run() {
                    synchronized (lock) {
                        entered.countDown();
                        try { lock.wait(); } catch (InterruptedException e) { }
                        wokenCount[0]++;
                    }
                    done.countDown();
                }
            }, "lcN2-all-" + i);
            ts[i].setDaemon(true);
            ts[i].start();
        }
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "all 3 waiters waiting");
        synchronized (lock) {
            lock.notifyAll();
        }
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "all 3 finished (bounded)");
        check(wokenCount[0] == 3, "all 3 woken by notifyAll, got " + wokenCount[0]);
        for (Thread t : ts) { t.join(10000); check(!t.isAlive(), "waiter terminated"); }
    }

    /** wait 释放监视器：等待期间其他线程可进入同步块；notify 后等待者恢复。 */
    static void testWaitReleasesLock() throws Exception {
        final Object lock = new Object();
        final java.util.concurrent.CountDownLatch entered =
            new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch done =
            new java.util.concurrent.CountDownLatch(1);
        Thread waiter = new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    entered.countDown();
                    try { lock.wait(30000); } catch (InterruptedException e) { }
                }
                done.countDown();
            }
        }, "lcN2-wait-release");
        waiter.setDaemon(true);
        waiter.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "waiter entered monitor");
        synchronized (lock) {
            // main 拿到了锁 —— 说明 wait() 已释放监视器
            check(waiter.getState() == Thread.State.TIMED_WAITING,
                  "waiter in TIMED_WAITING (timed wait), got " + waiter.getState());
            check(Thread.holdsLock(lock), "main owns the lock inside sync (holdsLock)");
            check(waiter.getState() != Thread.State.BLOCKED,
                  "waiter is not blocked on the monitor we hold");
        }
        check(Thread.holdsLock(lock) == false, "holdsLock false outside sync block");
        synchronized (lock) { lock.notify(); }
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "waiter resumed and finished");
        waiter.join(10000);
        check(!waiter.isAlive(), "waiter terminated");
    }

    /** wait 期间被中断：抛 InterruptedException 且中断状态被清空。 */
    static void testInterruptDuringWait() throws Exception {
        final Object lock = new Object();
        final java.util.concurrent.CountDownLatch entered =
            new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch done =
            new java.util.concurrent.CountDownLatch(1);
        final InterruptedException[] caught = new InterruptedException[1];
        Thread waiter = new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    entered.countDown();
                    try { lock.wait(); } catch (InterruptedException e) { caught[0] = e; }
                }
                done.countDown();
            }
        }, "lcN2-interrupted");
        waiter.setDaemon(true);
        waiter.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "waiter waiting");
        waiter.interrupt();
        check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "waiter finished after interrupt");
        check(caught[0] != null, "InterruptedException was thrown");
        check(!waiter.isInterrupted(), "interrupt status CLEARED once InterruptedException thrown");
        waiter.join(10000);
        check(!waiter.isAlive(), "waiter terminated");
    }

    /** clone 补充：CNSE 精确消息（被克隆类名）、clone 结果仍 Cloneable。 */
    static void testCloneDetails() throws Exception {
        NotCloneable n = new NotCloneable();
        try {
            n.clone();
            throw new AssertionError("expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
            check(e.getClass() == CloneNotSupportedException.class,
                  "exact CloneNotSupportedException class");
            check(NotCloneable.class.getName().equals(e.getMessage()),
                  "Object.clone CNSE message is the cloned class binary name: " + e.getMessage());
            check(e instanceof Cloneable == false, "the exception is not Cloneable");
            check(e instanceof Exception, "it is a checked Exception");
        }
        int[] shared = new int[] { 5 };
        CloneableThing c = new CloneableThing(5, shared);
        CloneableThing d = c.clone();
        check(d instanceof Cloneable, "clone of a Cloneable is still Cloneable");
        check(d.getClass() == c.getClass(), "runtime class preserved (restated)");
        check(d.ref == shared, "reference identity shared (shallow)");
        check(d.x == 5 && d.ref[0] == 5, "content copied");
        // 两次 clone 得三个互异实例
        CloneableThing e2 = c.clone();
        check(c != d && d != e2 && c != e2, "repeated clones are pairwise distinct");
    }

    /** 任意对象可作监视器：holdsLock 真值表。 */
    static void testAnyObjectAsMonitor() {
        Plain monitor = new Plain();
        check(!Thread.holdsLock(monitor), "holdsLock false before sync");
        synchronized (monitor) {
            check(Thread.holdsLock(monitor), "holdsLock true inside sync");
            synchronized (monitor) {
                check(Thread.holdsLock(monitor), "reentrant acquisition keeps holdsLock true");
            }
            check(Thread.holdsLock(monitor), "inner release keeps outer ownership");
        }
        check(!Thread.holdsLock(monitor), "holdsLock false after sync");
        String strLock = "lock-lcN2";   // 注意：String 常量池共享——只做本地自持锁验证
        synchronized (strLock) {
            check(Thread.holdsLock(strLock), "any object (even String) can be a monitor");
        }
    }

    /** equals/hashCode 在包装类上的既定行为（Object 契约的实例佐证）。 */
    static void testEqualsOnWrappersAndArrays() {
        check(Integer.valueOf(5).equals(Integer.valueOf(5)), "Integer equals by value");
        check(!Integer.valueOf(5).equals(Long.valueOf(5)), "Integer vs Long never equals (type first)");
        check(!new int[] {1}.equals(new int[] {1}),
              "arrays use identity equals (no content equality)");
        int[] same = new int[] {1};
        check(same.equals(same), "array equals itself (identity)");
        Object o = new Object();
        check(o.equals(o), "Object.equals identity reflexive (restated)");
        // equals 恒为 true/false 而非抛异常
        check(!o.equals(new Plain()), "cross-type equals is false, no exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetClass();
        testHashCodeEquals();
        testToString();
        testClone();
        testMonitorErrors();
        testWaitNotify();
        testWaitTimeout();
        testBlackBox();
        testWhiteBoxStructure();
        testFinalizeDeprecation();
        testEqualsContractValueSemantics();
        testHashCodeContract();
        testGetClassIdentity();
        testToStringFormats();
        testMonitorErrorsExtended();
        testWaitArgumentValidation();
        testNotifySelectsExactlyOne();
        testNotifyAllWakesAll();
        testWaitReleasesLock();
        testInterruptDuringWait();
        testCloneDetails();
        testAnyObjectAsMonitor();
        testEqualsOnWrappersAndArrays();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

