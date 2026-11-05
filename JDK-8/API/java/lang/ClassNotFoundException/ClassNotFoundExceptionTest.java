/*
 * @test
 * @summary java.lang.ClassNotFoundException API conformance: ReflectiveOperationException
 *          hierarchy, no-arg/(String)/(String,Throwable) constructors, and getException()
 *          returning the cause.
 * @run main ClassNotFoundExceptionTest
 */

public class ClassNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ClassNotFoundException e = new ClassNotFoundException();
        check(e instanceof ReflectiveOperationException, "CNFE is a ReflectiveOperationException");
        check(e instanceof Exception, "CNFE is an Exception");
        check(!((Object) e instanceof RuntimeException), "CNFE is checked (not RuntimeException)");
    }

    static void testConstructors() {
        ClassNotFoundException e1 = new ClassNotFoundException();
        check(e1.getMessage() == null && e1.getException() == null, "no-arg ctor");
        ClassNotFoundException e2 = new ClassNotFoundException("C");
        check("C".equals(e2.getMessage()) && e2.getException() == null, "(String) ctor");
        Throwable cause = new Throwable("cause");
        ClassNotFoundException e3 = new ClassNotFoundException("C", cause);
        check("C".equals(e3.getMessage()), "(String,Throwable) ctor message");
        check(e3.getException() == cause, "getException returns the cause");
        check(e3.getCause() == cause, "getCause returns the cause");
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { ClassNotFoundException e = new ClassNotFoundException();
          check(e.getMessage() == null, "ClassNotFoundException () message null");
          check(e.getException() == null, "ClassNotFoundException () getException null");
          check("java.lang.ClassNotFoundException".equals(e.toString()), "ClassNotFoundException () toString exact");
          check(e.getClass() == ClassNotFoundException.class, "ClassNotFoundException () exact class"); }
        { ClassNotFoundException e = new ClassNotFoundException("m");
          check("m".equals(e.getMessage()), "ClassNotFoundException (String) message");
          check(e.getException() == null, "ClassNotFoundException (String) getException null");
          check(e.getCause() == null, "ClassNotFoundException (String) getCause null");
          check("java.lang.ClassNotFoundException: m".equals(e.toString()), "ClassNotFoundException (String) toString exact"); }
        { Exception root = new Exception("root");
          ClassNotFoundException e = new ClassNotFoundException("m", root);
          check("m".equals(e.getMessage()), "ClassNotFoundException (String,Throwable) message");
          check(e.getException() == root, "ClassNotFoundException (String,Throwable) getException same ref");
          check(e.getCause() == root, "ClassNotFoundException (String,Throwable) getCause same ref");
          check(e.getException() == e.getCause(), "getException() and getCause() are the same object"); }
        check(new ClassNotFoundException((String) null).getMessage() == null, "ClassNotFoundException (null String) message null");
        check("java.lang.ClassNotFoundException".equals(new ClassNotFoundException((String) null).toString()), "ClassNotFoundException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 ()、(String)、(String,Throwable)，无单 (Throwable)）
        try { ClassNotFoundException.class.getConstructor(Throwable.class); check(false, "ClassNotFoundException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "ClassNotFoundException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        ClassNotFoundException t = new ClassNotFoundException("m");
        // ---- cause 生命周期（CNFE 特化：构造器已把 cause 位置显式占位，initCause 永远拒绝，见 testTriggerDeep） ----
        check(t.getCause() == null, "cause defaults to null");
        try {
            t.initCause(new RuntimeException("x"));
            check(false, "CNFE initCause is always rejected");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with java.lang.RuntimeException: x"),
                  "contract-segment initCause ISE message exact: " + e.getMessage());
        }
        { ClassNotFoundException s = new ClassNotFoundException("s");
          try { s.initCause(s); check(false, "CNFE initCause(self) must throw"); }
          catch (IllegalStateException e) { check(true, "self-initCause hits ISE branch (not the IAE branch)"); } }
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
        check(ClassNotFoundExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.ClassNotFoundException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + ClassNotFoundExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.ClassNotFoundException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ClassNotFoundException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == ClassNotFoundException.class, "lookup.forClass identity");
        check("java.lang.ClassNotFoundException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == 9176873029745254542L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        ClassNotFoundException e = new ClassNotFoundException("m");
        Object o = e;
        check(o instanceof ReflectiveOperationException, "is ReflectiveOperationException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(!(o instanceof RuntimeException), "checked exception: not a RuntimeException");
        check(!(o instanceof Error), "not an Error");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof NoSuchMethodException), "sibling separation: not NoSuchMethodException");
        check(!(o instanceof IllegalAccessException), "sibling separation: not IllegalAccessException");
    }

    // 特有成员 getException() 与「构造即钉死 cause、initCause 永远拒绝」契约 + forName 真触发
    static void testTriggerDeep() throws Exception {
        // initCause 在全部三种构造形态下都被拒绝（构造器把 cause 位置显式占位）
        ClassNotFoundException a = new ClassNotFoundException();
        try { a.initCause(new Exception("x")); check(false, "() initCause must be rejected"); }
        catch (IllegalStateException e) { check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with java.lang.Exception: x"), "() initCause ISE message"); }
        ClassNotFoundException b = new ClassNotFoundException("m");
        try { b.initCause(new Exception("x")); check(false, "(String) initCause must be rejected"); }
        catch (IllegalStateException e) { check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with java.lang.Exception: x"), "(String) initCause ISE message"); }
        Exception root = new Exception("root");
        ClassNotFoundException cc = new ClassNotFoundException("m", root);
        try { cc.initCause(new Exception("x")); check(false, "(String,Throwable) initCause must be rejected"); }
        catch (IllegalStateException e) { check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with java.lang.Exception: x"), "(String,Throwable) initCause ISE message"); }
        // initCause(self)：普通异常抛 IAE（Self-causation），CNFE 因 cause 已被占位先抛 ISE
        try {
            cc.initCause(cc);
            check(false, "CNFE initCause(self) must throw");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with java.lang.ClassNotFoundException: m"),
                  "self-initCause hits the ISE branch before the IAE branch");
        } catch (IllegalArgumentException e) {
            check(false, "unexpected IAE: overwrite check must run first");
        }
        // 真触发：Class.forName 不存在的二元名
        try {
            Class.forName("no.such.Cls");
            check(false, "forName(missing) must throw");
        } catch (ClassNotFoundException e) {
            check(e.getClass() == ClassNotFoundException.class, "forName exact type");
            check("no.such.Cls".equals(e.getMessage()), "forName message is the binary name verbatim");
            check("java.lang.ClassNotFoundException: no.such.Cls".equals(e.toString()), "forName toString exact");
            check(e.getException() == null, "forName-thrown instance getException null");
            check(e.getCause() == null, "forName-thrown instance getCause null");
        }
        // 三参 forName 形态消息一致
        try {
            Class.forName("no.such.Cls2", false, ClassNotFoundExceptionTest.class.getClassLoader());
            check(false, "forName(name,false,loader) must throw");
        } catch (ClassNotFoundException e) {
            check("no.such.Cls2".equals(e.getMessage()), "three-arg forName message identical");
        }
        // 原始类型名不是二元类名
        try {
            Class.forName("int");
            check(false, "forName(primitive name) must throw");
        } catch (ClassNotFoundException e) {
            check("int".equals(e.getMessage()), "primitive name message verbatim");
        }
        // 数组元素类缺失：消息剥离数组前缀，只报元素类
        try {
            Class.forName("[Lno.such.Cls;");
            check(false, "forName(missing element array) must throw");
        } catch (ClassNotFoundException e) {
            check("no.such.Cls".equals(e.getMessage()), "array prefix stripped in message (measured)");
        }
        // 对照：基本类型数组名合法
        check(Class.forName("[I") == int[].class, "contrast: [I resolves to int[].class");
        check(Class.forName("[I").isArray(), "resolved class reports isArray");
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
