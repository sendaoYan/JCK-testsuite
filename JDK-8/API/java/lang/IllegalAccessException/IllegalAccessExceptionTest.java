/*
 * @test
 * @summary java.lang.IllegalAccessException API conformance: ReflectiveOperationException
 *          hierarchy, checked status, and the no-arg/(String) constructors.
 * @run main IllegalAccessExceptionTest
 */

public class IllegalAccessExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        IllegalAccessException e = new IllegalAccessException();
        check(e instanceof ReflectiveOperationException, "IAE is a ReflectiveOperationException");
        check(e instanceof Exception, "IAE is an Exception");
        check(!((Object) e instanceof RuntimeException), "IAE is checked (not RuntimeException)");
    }

    static void testConstructors() {
        IllegalAccessException e1 = new IllegalAccessException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        IllegalAccessException e2 = new IllegalAccessException("m");
        check("m".equals(e2.getMessage()), "(String) ctor message");
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { IllegalAccessException e = new IllegalAccessException();
          check(e.getMessage() == null, "IllegalAccessException () message null");
          check(e.getCause() == null, "IllegalAccessException () cause null");
          check("java.lang.IllegalAccessException".equals(e.toString()), "IllegalAccessException () toString exact");
          check(e.getClass() == IllegalAccessException.class, "IllegalAccessException () exact class"); }
        { IllegalAccessException e = new IllegalAccessException("m");
          check("m".equals(e.getMessage()), "IllegalAccessException (String) message");
          check(e.getCause() == null, "IllegalAccessException (String) cause null");
          check("java.lang.IllegalAccessException: m".equals(e.toString()), "IllegalAccessException (String) toString exact"); }
        check(new IllegalAccessException((String) null).getMessage() == null, "IllegalAccessException (null String) message null");
        check("java.lang.IllegalAccessException".equals(new IllegalAccessException((String) null).toString()), "IllegalAccessException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 () 与 (String) 两个公共构造器，无 cause 通道构造器）
        try { IllegalAccessException.class.getConstructor(String.class, Throwable.class); check(false, "IllegalAccessException must not expose (String,Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "IllegalAccessException has no (String,Throwable) ctor"); }
        try { IllegalAccessException.class.getConstructor(Throwable.class); check(false, "IllegalAccessException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "IllegalAccessException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        IllegalAccessException t = new IllegalAccessException("m");
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
        { IllegalAccessException s = new IllegalAccessException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { IllegalAccessException s = new IllegalAccessException("s");
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
        check(IllegalAccessExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.IllegalAccessException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + IllegalAccessExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.IllegalAccessException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(IllegalAccessException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == IllegalAccessException.class, "lookup.forClass identity");
        check("java.lang.IllegalAccessException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == 6616958222490762034L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        IllegalAccessException e = new IllegalAccessException("m");
        Object o = e;
        check(o instanceof ReflectiveOperationException, "is ReflectiveOperationException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(!(o instanceof RuntimeException), "checked exception: not a RuntimeException");
        check(!(o instanceof Error), "not an Error");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof InstantiationException), "sibling separation: not InstantiationException");
        check(!(o instanceof ClassNotFoundException), "sibling separation: not ClassNotFoundException");
    }

    // 真触发：跨 nest 访问 java.lang.Void 的私有构造器（不 setAccessible）
    static void testTriggerDeep() throws Exception {
        java.lang.reflect.Constructor<java.lang.Void> vc = java.lang.Void.class.getDeclaredConstructor();
        check(vc != null, "getDeclaredConstructor itself succeeds for a private ctor");
        try {
            vc.newInstance();
            check(false, "newInstance on private ctor without setAccessible must throw");
        } catch (IllegalAccessException e) {
            check(e.getClass() == IllegalAccessException.class, "exact type IllegalAccessException");
            check(e.getMessage() != null && e.getMessage().contains("java.lang.Void"),
                  "message mentions the target class: " + e.getMessage());
            if (MODERN) {
                check(e.getMessage().contains("cannot access a member of class java.lang.Void")
                      && e.getMessage().contains("(in module java.base)"),
                      "27 message shape: cannot access + module info");
            } else {
                check(e.getMessage().contains("can not access a member of class java.lang.Void")
                      && e.getMessage().contains("with modifiers \"private\""),
                      "8 message shape: can not access + modifiers \"private\"");
            }
            check(e.getCause() == null, "triggered instance has null cause");
        }
        // 对照：对本测试自己的私有构造器 setAccessible(true) 打开访问后可实例化
        // （自己的类不受 27 的模块强封装限制；对 java.lang.Void 这样做在 27 会抛 InaccessibleObjectException）
        java.lang.reflect.Constructor<PrivateCtorSample> own = PrivateCtorSample.class.getDeclaredConstructor();
        own.setAccessible(true);
        own.setAccessible(true); // 幂等
        check(own.newInstance() != null, "own private ctor instantiates after setAccessible(true)");
        // 注：不测 setAccessible(false) 后再 newInstance——JDK-11 起 nestmates 让同文件嵌套类
        // 私有构造即使不开放也可直接 newInstance，而 JDK-8 需要，两侧行为分叉，不在此断言。
    }

    static class PrivateCtorSample {
        private PrivateCtorSample() { }
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testConstructors();
        // lcO2 契约深化：构造器/Throwable 契约/序列化/层级/真触发
        testCtorExact();
        testThrowableContract();
        testSerialIdentity();
        testHierarchyExact();
        testTriggerDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
