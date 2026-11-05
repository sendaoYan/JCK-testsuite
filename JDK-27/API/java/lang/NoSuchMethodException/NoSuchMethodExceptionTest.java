/*
 * @test
 * @summary java.lang.NoSuchMethodException conformance: white-box structure + black-box behavior.
 * @run main NoSuchMethodExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NoSuchMethodExceptionTest {
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
        Class<?> c = Class.forName("java.lang.NoSuchMethodException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == ReflectiveOperationException.class, "superclass is ReflectiveOperationException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.NoSuchMethodException");

        NoSuchMethodException e0 = new NoSuchMethodException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { NoSuchMethodException e = new NoSuchMethodException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        NoSuchMethodException t = new NoSuchMethodException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.NoSuchMethodException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: getMethod of missing method
        try {
            String.class.getMethod("noSuchMethodJck", new Class[0]);
            throw new AssertionError("trigger: expected NoSuchMethodException");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { NoSuchMethodException e = new NoSuchMethodException();
          check(e.getMessage() == null, "NoSuchMethodException () message null");
          check(e.getCause() == null, "NoSuchMethodException () cause null");
          check("java.lang.NoSuchMethodException".equals(e.toString()), "NoSuchMethodException () toString exact");
          check(e.getClass() == NoSuchMethodException.class, "NoSuchMethodException () exact class"); }
        { NoSuchMethodException e = new NoSuchMethodException("m");
          check("m".equals(e.getMessage()), "NoSuchMethodException (String) message");
          check(e.getCause() == null, "NoSuchMethodException (String) cause null");
          check("java.lang.NoSuchMethodException: m".equals(e.toString()), "NoSuchMethodException (String) toString exact"); }
        check(new NoSuchMethodException((String) null).getMessage() == null, "NoSuchMethodException (null String) message null");
        check("java.lang.NoSuchMethodException".equals(new NoSuchMethodException((String) null).toString()), "NoSuchMethodException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 () 与 (String) 两个公共构造器，无 cause 通道构造器）
        try { NoSuchMethodException.class.getConstructor(String.class, Throwable.class); check(false, "NoSuchMethodException must not expose (String,Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "NoSuchMethodException has no (String,Throwable) ctor"); }
        try { NoSuchMethodException.class.getConstructor(Throwable.class); check(false, "NoSuchMethodException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "NoSuchMethodException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        NoSuchMethodException t = new NoSuchMethodException("m");
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
        { NoSuchMethodException s = new NoSuchMethodException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { NoSuchMethodException s = new NoSuchMethodException("s");
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
        check(NoSuchMethodExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.NoSuchMethodException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + NoSuchMethodExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.NoSuchMethodException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(NoSuchMethodException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == NoSuchMethodException.class, "lookup.forClass identity");
        check("java.lang.NoSuchMethodException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == 5034388446362600923L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        NoSuchMethodException e = new NoSuchMethodException("m");
        Object o = e;
        check(o instanceof ReflectiveOperationException, "is ReflectiveOperationException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(!(o instanceof RuntimeException), "checked exception: not a RuntimeException");
        check(!(o instanceof Error), "not an Error");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof NoSuchFieldException), "sibling separation: not NoSuchFieldException");
        check(!(o instanceof InstantiationException), "sibling separation: not InstantiationException");
    }

    // 真触发：getMethod/getConstructor 找不到方法，消息为「类.名(参数表)」形态（逐字+版本分支）
    static void testTriggerDeep() throws Exception {
        try {
            java.lang.String.class.getMethod("nope");
            check(false, "getMethod(missing) must throw");
        } catch (NoSuchMethodException e) {
            check(e.getClass() == NoSuchMethodException.class, "getMethod exact type");
            check("java.lang.String.nope()".equals(e.getMessage()), "no-arg message form is Class.name() verbatim");
            check("java.lang.NoSuchMethodException: java.lang.String.nope()".equals(e.toString()), "getMethod toString exact");
            check(e.getCause() == null, "getMethod-thrown cause null");
        }
        // 带参数签名：JDK-8 参数表逗号后带空格，JDK-9+ 起无空格（实测锁定）
        try {
            java.lang.String.class.getMethod("charAt", int.class, int.class);
            check(false, "getMethod(missing 2-arg) must throw");
        } catch (NoSuchMethodException e) {
            String want = MODERN ? "java.lang.String.charAt(int,int)" : "java.lang.String.charAt(int, int)";
            check(want.equals(e.getMessage()), "parameter-list spacing locked per side: " + e.getMessage());
        }
        // 接口没有构造器：getDeclaredConstructor 报 <init> 形态消息
        try {
            Runnable.class.getDeclaredConstructor();
            check(false, "interface getDeclaredConstructor must throw");
        } catch (NoSuchMethodException e) {
            check("java.lang.Runnable.<init>()".equals(e.getMessage()), "constructor message uses <init> (measured both JDKs)");
        }
        // getConstructor 消息同类形态（前缀精确锁定）
        try {
            java.lang.String.class.getConstructor(CharSequence.class);
            check(false, "String has no public ctor taking CharSequence");
        } catch (NoSuchMethodException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("java.lang.String.<init>("),
                  "getConstructor message prefix: " + e.getMessage());
        }
        // 对照：存在的方法/构造器正常返回
        check(java.lang.String.class.getMethod("length") != null, "contrast: existing method found");
        check(java.lang.String.class.getMethod("charAt", int.class) != null, "contrast: existing 1-arg method found");
        check(java.lang.String.class.getConstructor(String.class) != null, "contrast: existing public ctor found");
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
