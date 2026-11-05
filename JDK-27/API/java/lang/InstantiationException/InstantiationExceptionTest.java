/*
 * @test
 * @summary java.lang.InstantiationException conformance: white-box structure + black-box behavior.
 * @run main InstantiationExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class InstantiationExceptionTest {
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
        Class<?> c = Class.forName("java.lang.InstantiationException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == ReflectiveOperationException.class, "superclass is ReflectiveOperationException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.InstantiationException");

        InstantiationException e0 = new InstantiationException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { InstantiationException e = new InstantiationException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        InstantiationException t = new InstantiationException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.InstantiationException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { InstantiationException e = new InstantiationException();
          check(e.getMessage() == null, "InstantiationException () message null");
          check(e.getCause() == null, "InstantiationException () cause null");
          check("java.lang.InstantiationException".equals(e.toString()), "InstantiationException () toString exact");
          check(e.getClass() == InstantiationException.class, "InstantiationException () exact class"); }
        { InstantiationException e = new InstantiationException("m");
          check("m".equals(e.getMessage()), "InstantiationException (String) message");
          check(e.getCause() == null, "InstantiationException (String) cause null");
          check("java.lang.InstantiationException: m".equals(e.toString()), "InstantiationException (String) toString exact"); }
        check(new InstantiationException((String) null).getMessage() == null, "InstantiationException (null String) message null");
        check("java.lang.InstantiationException".equals(new InstantiationException((String) null).toString()), "InstantiationException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 () 与 (String) 两个公共构造器，无 cause 通道构造器）
        try { InstantiationException.class.getConstructor(String.class, Throwable.class); check(false, "InstantiationException must not expose (String,Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "InstantiationException has no (String,Throwable) ctor"); }
        try { InstantiationException.class.getConstructor(Throwable.class); check(false, "InstantiationException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "InstantiationException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        InstantiationException t = new InstantiationException("m");
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
        { InstantiationException s = new InstantiationException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { InstantiationException s = new InstantiationException("s");
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
        check(InstantiationExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.InstantiationException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + InstantiationExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.InstantiationException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(InstantiationException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == InstantiationException.class, "lookup.forClass identity");
        check("java.lang.InstantiationException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == -8441929162975509110L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        InstantiationException e = new InstantiationException("m");
        Object o = e;
        check(o instanceof ReflectiveOperationException, "is ReflectiveOperationException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(!(o instanceof RuntimeException), "checked exception: not a RuntimeException");
        check(!(o instanceof Error), "not an Error");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof IllegalAccessException), "sibling separation: not IllegalAccessException");
        check(!(o instanceof NoSuchMethodException), "sibling separation: not NoSuchMethodException");
    }

    static abstract class AbstractSample { }
    static class ConcreteSample { }

    // 真触发：抽象类（自造嵌套 + java.lang.Number）newInstance 抛 InstantiationException
    static void testTriggerDeep() throws Exception {
        try {
            java.lang.reflect.Constructor<AbstractSample> c = AbstractSample.class.getDeclaredConstructor();
            c.newInstance();
            check(false, "newInstance on abstract class must throw");
        } catch (InstantiationException e) {
            check(e.getClass() == InstantiationException.class, "abstract nested class throws exact type");
            check(e.getMessage() == null, "triggered message is null on both JDKs (measured)");
            check(e.getCause() == null, "triggered cause null");
            check("java.lang.InstantiationException".equals(e.toString()), "toString exact with null message");
        }
        try {
            java.lang.reflect.Constructor<Number> cn = Number.class.getDeclaredConstructor();
            cn.newInstance();
            check(false, "newInstance on java.lang.Number must throw");
        } catch (InstantiationException e) {
            check(e.getClass() == InstantiationException.class, "java.lang.Number abstract path exact type");
            check(e.getMessage() == null, "Number path message null");
        }
        // 对照：具体类正常实例化（不是 IE 路径）
        Object made = ConcreteSample.class.getDeclaredConstructor().newInstance();
        check(made instanceof ConcreteSample, "contrast: concrete nested class instantiates fine");
        // 对照：接口根本没有构造器，走 NoSuchMethodException 而非 InstantiationException
        try {
            Runnable.class.getDeclaredConstructor();
            check(false, "interface has no constructor, expect NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            check(true, "interface path is NSME, not InstantiationException");
        }
        check(java.lang.reflect.Modifier.isAbstract(AbstractSample.class.getModifiers()), "sample is really abstract");
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
