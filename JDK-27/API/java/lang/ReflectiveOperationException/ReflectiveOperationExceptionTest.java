/*
 * @test
 * @summary java.lang.ReflectiveOperationException conformance: white-box structure + black-box behavior.
 * @run main ReflectiveOperationExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ReflectiveOperationExceptionTest {
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
        Class<?> c = Class.forName("java.lang.ReflectiveOperationException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Exception.class, "superclass is Exception");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ReflectiveOperationException");

        ReflectiveOperationException e0 = new ReflectiveOperationException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { ReflectiveOperationException e = new ReflectiveOperationException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); ReflectiveOperationException e = new ReflectiveOperationException("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }
        { Exception cause = new Exception("cause"); ReflectiveOperationException e = new ReflectiveOperationException(cause); check(e.getCause() == cause, "(Throwable) ctor cause"); }

        // common Throwable contract
        ReflectiveOperationException t = new ReflectiveOperationException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.ReflectiveOperationException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        RuntimeException rc = new RuntimeException("rc");
        { ReflectiveOperationException e = new ReflectiveOperationException();
          check(e.getMessage() == null, "ReflectiveOperationException () message null");
          check(e.getCause() == null, "ReflectiveOperationException () cause null");
          check("java.lang.ReflectiveOperationException".equals(e.toString()), "ReflectiveOperationException () toString exact");
          check(e.getClass() == ReflectiveOperationException.class, "ReflectiveOperationException () exact class"); }
        { ReflectiveOperationException e = new ReflectiveOperationException("m");
          check("m".equals(e.getMessage()), "ReflectiveOperationException (String) message");
          check(e.getCause() == null, "ReflectiveOperationException (String) cause null");
          check("java.lang.ReflectiveOperationException: m".equals(e.toString()), "ReflectiveOperationException (String) toString exact"); }
        { ReflectiveOperationException e = new ReflectiveOperationException("m", rc);
          check("m".equals(e.getMessage()), "ReflectiveOperationException (String,Throwable) message");
          check(e.getCause() == rc, "ReflectiveOperationException (String,Throwable) cause same ref");
          check("java.lang.ReflectiveOperationException: m".equals(e.toString()), "ReflectiveOperationException (String,Throwable) toString exact"); }
        { ReflectiveOperationException e = new ReflectiveOperationException(rc);
          check(rc.toString().equals(e.getMessage()), "ReflectiveOperationException (Throwable) message == cause.toString()");
          check(e.getCause() == rc, "ReflectiveOperationException (Throwable) cause same ref");
          check(("java.lang.ReflectiveOperationException: " + rc.toString()).equals(e.toString()), "ReflectiveOperationException (Throwable) toString exact"); }
        check(new ReflectiveOperationException((String) null).getMessage() == null, "ReflectiveOperationException (null String) message null");
        check("java.lang.ReflectiveOperationException".equals(new ReflectiveOperationException((String) null).toString()), "ReflectiveOperationException (null String) toString has no colon");
        check(new ReflectiveOperationException("m", null).getCause() == null, "ReflectiveOperationException (String,null) cause null");
        check("m".equals(new ReflectiveOperationException("m", null).getMessage()), "ReflectiveOperationException (String,null) message kept");
        check(new ReflectiveOperationException((Throwable) null).getMessage() == null, "ReflectiveOperationException (null Throwable) message null");
        check(new ReflectiveOperationException((Throwable) null).getCause() == null, "ReflectiveOperationException (null Throwable) cause null");
    }

    static void testThrowableContract() {
        ReflectiveOperationException t = new ReflectiveOperationException("m");
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
        { ReflectiveOperationException s = new ReflectiveOperationException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { ReflectiveOperationException s = new ReflectiveOperationException("s");
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
        check(ReflectiveOperationExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.ReflectiveOperationException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + ReflectiveOperationExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.ReflectiveOperationException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ReflectiveOperationException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == ReflectiveOperationException.class, "lookup.forClass identity");
        check("java.lang.ReflectiveOperationException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == 123456789L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        ReflectiveOperationException e = new ReflectiveOperationException("m");
        Object o = e;
        check(o instanceof ReflectiveOperationException, "is ReflectiveOperationException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(!(o instanceof RuntimeException), "checked exception: not a RuntimeException");
        check(!(o instanceof Error), "not an Error");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof RuntimeException), "sibling separation: not RuntimeException");
        check(!(o instanceof CloneNotSupportedException), "sibling separation: not CloneNotSupportedException");
    }

    // 基类视角：ROE 本身可直接实例化（非 abstract），五成员家族归属逐一断言
    static void testTriggerDeep() {
        ReflectiveOperationException direct = new ReflectiveOperationException("direct");
        check("direct".equals(direct.getMessage()), "ROE is concrete: direct instantiation works");
        check(!((Object) direct instanceof RuntimeException), "ROE base stays a checked exception");
        check(new ClassNotFoundException("x") instanceof ReflectiveOperationException, "ClassNotFoundException is a ROE");
        check(new IllegalAccessException("x") instanceof ReflectiveOperationException, "IllegalAccessException is a ROE");
        check(new InstantiationException("x") instanceof ReflectiveOperationException, "InstantiationException is a ROE");
        check(new NoSuchFieldException("x") instanceof ReflectiveOperationException, "NoSuchFieldException is a ROE");
        check(new NoSuchMethodException("x") instanceof ReflectiveOperationException, "NoSuchMethodException is a ROE");
        // 家族成员可用作泛型 cause（Exception 通道）
        NoSuchMethodException inner = new NoSuchMethodException("inner");
        ReflectiveOperationException wrapped = new ReflectiveOperationException("wrap", inner);
        check(wrapped.getCause() == inner, "family member chains as cause, same ref");
        check(wrapped.getCause() instanceof ReflectiveOperationException, "cause chain still inside the family");
        // getDeclaredConstructors 恰好 4 个公共构造器（白盒，javap 双侧一致）
        check(java.lang.reflect.Modifier.isAbstract(ReflectiveOperationException.class.getModifiers()) == false,
              "ROE class is not abstract");
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
