/*
 * @test
 * @summary java.lang.NoSuchFieldException conformance: white-box structure + black-box behavior.
 * @run main NoSuchFieldExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NoSuchFieldExceptionTest {
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
        Class<?> c = Class.forName("java.lang.NoSuchFieldException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == ReflectiveOperationException.class, "superclass is ReflectiveOperationException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.NoSuchFieldException");

        NoSuchFieldException e0 = new NoSuchFieldException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { NoSuchFieldException e = new NoSuchFieldException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        NoSuchFieldException t = new NoSuchFieldException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.NoSuchFieldException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: getField of missing field
        try {
            String.class.getField("noSuchFieldJck");
            throw new AssertionError("trigger: expected NoSuchFieldException");
        } catch (NoSuchFieldException expected) {
            passed++;
        }
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { NoSuchFieldException e = new NoSuchFieldException();
          check(e.getMessage() == null, "NoSuchFieldException () message null");
          check(e.getCause() == null, "NoSuchFieldException () cause null");
          check("java.lang.NoSuchFieldException".equals(e.toString()), "NoSuchFieldException () toString exact");
          check(e.getClass() == NoSuchFieldException.class, "NoSuchFieldException () exact class"); }
        { NoSuchFieldException e = new NoSuchFieldException("m");
          check("m".equals(e.getMessage()), "NoSuchFieldException (String) message");
          check(e.getCause() == null, "NoSuchFieldException (String) cause null");
          check("java.lang.NoSuchFieldException: m".equals(e.toString()), "NoSuchFieldException (String) toString exact"); }
        check(new NoSuchFieldException((String) null).getMessage() == null, "NoSuchFieldException (null String) message null");
        check("java.lang.NoSuchFieldException".equals(new NoSuchFieldException((String) null).toString()), "NoSuchFieldException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 () 与 (String) 两个公共构造器，无 cause 通道构造器）
        try { NoSuchFieldException.class.getConstructor(String.class, Throwable.class); check(false, "NoSuchFieldException must not expose (String,Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "NoSuchFieldException has no (String,Throwable) ctor"); }
        try { NoSuchFieldException.class.getConstructor(Throwable.class); check(false, "NoSuchFieldException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "NoSuchFieldException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        NoSuchFieldException t = new NoSuchFieldException("m");
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
        { NoSuchFieldException s = new NoSuchFieldException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { NoSuchFieldException s = new NoSuchFieldException("s");
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
        check(NoSuchFieldExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.NoSuchFieldException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + NoSuchFieldExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.NoSuchFieldException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(NoSuchFieldException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == NoSuchFieldException.class, "lookup.forClass identity");
        check("java.lang.NoSuchFieldException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == -6143714805279938260L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        NoSuchFieldException e = new NoSuchFieldException("m");
        Object o = e;
        check(o instanceof ReflectiveOperationException, "is ReflectiveOperationException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(!(o instanceof RuntimeException), "checked exception: not a RuntimeException");
        check(!(o instanceof Error), "not an Error");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof NoSuchMethodException), "sibling separation: not NoSuchMethodException");
        check(!(o instanceof ClassNotFoundException), "sibling separation: not ClassNotFoundException");
    }

    // 真触发：getField/getDeclaredField 找不到字段，消息就是字段名本身（逐字）
    static void testTriggerDeep() throws Exception {
        try {
            java.lang.String.class.getField("NOPE");
            check(false, "getField(missing) must throw");
        } catch (NoSuchFieldException e) {
            check(e.getClass() == NoSuchFieldException.class, "getField exact type");
            check("NOPE".equals(e.getMessage()), "getField message is the field name verbatim (measured both JDKs)");
            check("java.lang.NoSuchFieldException: NOPE".equals(e.toString()), "getField toString exact");
            check(e.getCause() == null, "getField-thrown cause null");
        }
        try {
            java.lang.String.class.getDeclaredField("NOPE2");
            check(false, "getDeclaredField(missing) must throw");
        } catch (NoSuchFieldException e) {
            check(e.getClass() == NoSuchFieldException.class, "getDeclaredField exact type");
            check("NOPE2".equals(e.getMessage()), "getDeclaredField message verbatim");
        }
        // 对照：存在的字段（public 与 declared 各一条）不抛
        check(java.lang.String.class.getField("CASE_INSENSITIVE_ORDER") != null, "contrast: existing public field found");
        check(java.lang.String.class.getDeclaredField("value") != null, "contrast: existing declared field found");
        check("NOPE".equals(new NoSuchFieldException("NOPE").getMessage()), "(String) ctor roundtrip message");
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
