/*
 * @test
 * @summary java.lang.IllegalMonitorStateException conformance: white-box structure + black-box behavior.
 * @run main IllegalMonitorStateExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IllegalMonitorStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IllegalMonitorStateException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IllegalMonitorStateException");

        IllegalMonitorStateException e0 = new IllegalMonitorStateException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { IllegalMonitorStateException e = new IllegalMonitorStateException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        IllegalMonitorStateException t = new IllegalMonitorStateException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.IllegalMonitorStateException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: wait without owning monitor
        try {
            new Object().wait();
            throw new AssertionError("trigger: expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException expected) {
            passed++;
        } catch (InterruptedException unexpected) {
            throw new AssertionError(unexpected);
        }
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { IllegalMonitorStateException e = new IllegalMonitorStateException();
          check(e.getMessage() == null, "IllegalMonitorStateException () message null");
          check(e.getCause() == null, "IllegalMonitorStateException () cause null");
          check("java.lang.IllegalMonitorStateException".equals(e.toString()), "IllegalMonitorStateException () toString exact");
          check(e.getClass() == IllegalMonitorStateException.class, "IllegalMonitorStateException () exact class"); }
        { IllegalMonitorStateException e = new IllegalMonitorStateException("m");
          check("m".equals(e.getMessage()), "IllegalMonitorStateException (String) message");
          check(e.getCause() == null, "IllegalMonitorStateException (String) cause null");
          check("java.lang.IllegalMonitorStateException: m".equals(e.toString()), "IllegalMonitorStateException (String) toString exact"); }
        check(new IllegalMonitorStateException((String) null).getMessage() == null, "IllegalMonitorStateException (null String) message null");
        check("java.lang.IllegalMonitorStateException".equals(new IllegalMonitorStateException((String) null).toString()), "IllegalMonitorStateException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 () 与 (String) 两个公共构造器，无 cause 通道构造器）
        try { IllegalMonitorStateException.class.getConstructor(String.class, Throwable.class); check(false, "IllegalMonitorStateException must not expose (String,Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "IllegalMonitorStateException has no (String,Throwable) ctor"); }
        try { IllegalMonitorStateException.class.getConstructor(Throwable.class); check(false, "IllegalMonitorStateException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "IllegalMonitorStateException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        IllegalMonitorStateException t = new IllegalMonitorStateException("m");
        // ---- cause 生命周期 ----
        check(t.getCause() == null, "cause defaults to null");
        RuntimeException rc = new RuntimeException("rc");
        check(t.initCause(rc) == t, "initCause returns this");
        check(t.getCause() == rc, "initCause stores cause same ref");
        try {
            t.initCause(new RuntimeException("x"));
            check(false, "second initCause must throw IllegalStateException");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with java.lang.RuntimeException: x"),
                  "second initCause ISE message exact: " + e.getMessage());
        }
        { IllegalMonitorStateException s = new IllegalMonitorStateException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { IllegalMonitorStateException s = new IllegalMonitorStateException("s");
          check(s.initCause(null) == s, "initCause(null) accepted, returns this");
          check(s.getCause() == null, "initCause(null) leaves cause null");
          try { s.initCause(rc); check(false, "initCause after initCause(null) still ISE"); }
          catch (IllegalStateException e) { check(true, "initCause(null) already consumes the one-shot cause slot"); } }
        // ---- suppressed 异常 ----
        check(t.getSuppressed().length == 0, "suppressed defaults to empty array");
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "suppressed count after two addSuppressed");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed order preserved");
        Throwable[] view = t.getSuppressed();
        view[0] = null;
        check(t.getSuppressed()[0] == s1, "getSuppressed returns defensive copy");
        try { t.addSuppressed(t); check(false, "addSuppressed(self) must throw IllegalArgumentException"); }
        catch (IllegalArgumentException e) { check("Self-suppression not permitted".equals(e.getMessage()), "addSuppressed(self) IAE exact message"); }
        try { t.addSuppressed(null); check(false, "addSuppressed(null) must throw NullPointerException"); }
        catch (NullPointerException e) { check("Cannot suppress a null exception.".equals(e.getMessage()), "addSuppressed(null) NPE exact message"); }
        // ---- 栈轨迹 ----
        check(t.getStackTrace().length >= 1, "captured stack trace non-empty");
        check(IllegalMonitorStateExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
        StackTraceElement f1 = new StackTraceElement("Fake", "go", "Fake.java", 7);
        StackTraceElement f2 = new StackTraceElement("Fake", "run", "Fake.java", 9);
        t.setStackTrace(new StackTraceElement[] { f1, f2 });
        check(t.getStackTrace().length == 2, "setStackTrace length readback");
        check("Fake".equals(t.getStackTrace()[0].getClassName()) && "go".equals(t.getStackTrace()[0].getMethodName())
              && "Fake.java".equals(t.getStackTrace()[0].getFileName()) && t.getStackTrace()[0].getLineNumber() == 7,
              "setStackTrace frame 0 all four fields readback");
        check("Fake".equals(t.getStackTrace()[1].getClassName()) && "run".equals(t.getStackTrace()[1].getMethodName())
              && "Fake.java".equals(t.getStackTrace()[1].getFileName()) && t.getStackTrace()[1].getLineNumber() == 9,
              "setStackTrace frame 1 all four fields readback");
        StackTraceElement[] st = t.getStackTrace();
        st[0] = null;
        check(t.getStackTrace()[0] != null && "Fake".equals(t.getStackTrace()[0].getClassName()),
              "getStackTrace returns defensive copy");
        t.setStackTrace(new StackTraceElement[0]);
        check(t.getStackTrace().length == 0, "setStackTrace(empty) readback empty");
        try { t.setStackTrace(null); check(false, "setStackTrace(null) must throw NullPointerException"); }
        catch (NullPointerException e) { check(true, "setStackTrace(null) NPE (helpful text differs per JDK, type only)"); }
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        // ---- 打印契约（内存流，无 stderr 副作用） ----
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String dump = bos.toString();
        check(dump.startsWith("java.lang.IllegalMonitorStateException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + IllegalMonitorStateExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.IllegalMonitorStateException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(IllegalMonitorStateException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == IllegalMonitorStateException.class, "lookup.forClass identity");
        check("java.lang.IllegalMonitorStateException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == 3713306369498869069L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        IllegalMonitorStateException e = new IllegalMonitorStateException("m");
        Object o = e;
        check(o instanceof RuntimeException, "is RuntimeException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof Error), "not an Error");
        check(!(o instanceof ReflectiveOperationException), "outside the reflective family");
        check(!(o instanceof IllegalStateException), "sibling separation: not IllegalStateException");
        check(!(o instanceof IllegalThreadStateException), "sibling separation: not IllegalThreadStateException");
    }

    // 真触发：未持有监视器锁时 wait/notify 全家族抛 IllegalMonitorStateException（自造对象）
    static void testTriggerDeep() {
        Object lock = new Object();
        String wantMsg = MODERN ? "current thread is not owner" : null;
        try { lock.wait(); check(false, "wait() without lock must throw"); }
        catch (InterruptedException ie) { check(false, "unexpected InterruptedException"); }
        catch (IllegalMonitorStateException e) {
            check(e.getClass() == IllegalMonitorStateException.class, "wait() throws exact type");
            check(wantMsg == null ? e.getMessage() == null : wantMsg.equals(e.getMessage()),
                  "wait() message locked per side: " + e.getMessage());
            check(e.getCause() == null, "wait() thrown instance has null cause");
            check(wantMsg == null ? "java.lang.IllegalMonitorStateException".equals(e.toString())
                                  : "java.lang.IllegalMonitorStateException: current thread is not owner".equals(e.toString()),
                  "wait() toString exact");
        }
        try { lock.wait(1); check(false, "wait(ms) without lock must throw"); }
        catch (InterruptedException ie) { check(false, "unexpected InterruptedException"); }
        catch (IllegalMonitorStateException e) {
            check(e.getClass() == IllegalMonitorStateException.class, "wait(ms) throws exact type");
            check(wantMsg == null ? e.getMessage() == null : wantMsg.equals(e.getMessage()),
                  "wait(ms) message locked per side");
        }
        try { lock.wait(1, 1); check(false, "wait(ms,ns) without lock must throw"); }
        catch (InterruptedException ie) { check(false, "unexpected InterruptedException"); }
        catch (IllegalMonitorStateException e) {
            check(e.getClass() == IllegalMonitorStateException.class, "wait(ms,ns) throws exact type");
            check(wantMsg == null ? e.getMessage() == null : wantMsg.equals(e.getMessage()),
                  "wait(ms,ns) message locked per side");
        }
        try { lock.notify(); check(false, "notify without lock must throw"); }
        catch (IllegalMonitorStateException e) {
            check(e.getClass() == IllegalMonitorStateException.class, "notify throws exact type");
            check(wantMsg == null ? e.getMessage() == null : wantMsg.equals(e.getMessage()),
                  "notify message locked per side");
        }
        try { lock.notifyAll(); check(false, "notifyAll without lock must throw"); }
        catch (IllegalMonitorStateException e) {
            check(e.getClass() == IllegalMonitorStateException.class, "notifyAll throws exact type");
            check(wantMsg == null ? e.getMessage() == null : wantMsg.equals(e.getMessage()),
                  "notifyAll message locked per side");
            check(e.getSuppressed().length == 0, "thrown instance has no suppressed");
        }
        // 对照：持锁时 notify 成功、退出块后不再持锁
        synchronized (lock) {
            lock.notify();
            check(true, "notify inside synchronized block never throws");
            check(Thread.holdsLock(lock), "holdsLock true inside synchronized");
        }
        check(!Thread.holdsLock(lock), "holdsLock false after synchronized block");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // lcO2 契约深化：构造器/Throwable 契约/序列化/层级/真触发
        testCtorExact();
        testThrowableContract();
        testSerialIdentity();
        testHierarchyExact();
        testTriggerDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
