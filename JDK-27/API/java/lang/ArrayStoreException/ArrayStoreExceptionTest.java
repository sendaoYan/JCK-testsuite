/*
 * @test
 * @summary java.lang.ArrayStoreException conformance: white-box structure + black-box behavior.
 * @run main ArrayStoreExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ArrayStoreExceptionTest {
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
        Class<?> c = Class.forName("java.lang.ArrayStoreException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ArrayStoreException");

        ArrayStoreException e0 = new ArrayStoreException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { ArrayStoreException e = new ArrayStoreException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        ArrayStoreException t = new ArrayStoreException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.ArrayStoreException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: storing incompatible element into array
        expect(ArrayStoreException.class, () -> { Object[] a = new String[1]; a[0] = Integer.valueOf(1); }, "trigger storing incompatible element into array");
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        { ArrayStoreException e = new ArrayStoreException();
          check(e.getMessage() == null, "ArrayStoreException () message null");
          check(e.getCause() == null, "ArrayStoreException () cause null");
          check("java.lang.ArrayStoreException".equals(e.toString()), "ArrayStoreException () toString exact");
          check(e.getClass() == ArrayStoreException.class, "ArrayStoreException () exact class"); }
        { ArrayStoreException e = new ArrayStoreException("m");
          check("m".equals(e.getMessage()), "ArrayStoreException (String) message");
          check(e.getCause() == null, "ArrayStoreException (String) cause null");
          check("java.lang.ArrayStoreException: m".equals(e.toString()), "ArrayStoreException (String) toString exact"); }
        check(new ArrayStoreException((String) null).getMessage() == null, "ArrayStoreException (null String) message null");
        check("java.lang.ArrayStoreException".equals(new ArrayStoreException((String) null).toString()), "ArrayStoreException (null String) toString has no colon");
        // 构造器面锁定（javap 双侧一致：仅 () 与 (String) 两个公共构造器，无 cause 通道构造器）
        try { ArrayStoreException.class.getConstructor(String.class, Throwable.class); check(false, "ArrayStoreException must not expose (String,Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "ArrayStoreException has no (String,Throwable) ctor"); }
        try { ArrayStoreException.class.getConstructor(Throwable.class); check(false, "ArrayStoreException must not expose (Throwable)"); }
        catch (NoSuchMethodException n) { check(true, "ArrayStoreException has no (Throwable) ctor"); }
    }

    static void testThrowableContract() {
        ArrayStoreException t = new ArrayStoreException("m");
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
        { ArrayStoreException s = new ArrayStoreException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { ArrayStoreException s = new ArrayStoreException("s");
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
        check(ArrayStoreExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.ArrayStoreException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + ArrayStoreExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.ArrayStoreException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ArrayStoreException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == ArrayStoreException.class, "lookup.forClass identity");
        check("java.lang.ArrayStoreException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == -4522193890499838241L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        ArrayStoreException e = new ArrayStoreException("m");
        Object o = e;
        check(o instanceof RuntimeException, "is RuntimeException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof Error), "not an Error");
        check(!(o instanceof ReflectiveOperationException), "outside the reflective family");
        check(!(o instanceof ClassCastException), "sibling separation: not ClassCastException");
        check(!(o instanceof NullPointerException), "sibling separation: not NullPointerException");
    }

    // 真触发：协变数组写入不兼容元素（aastore 检查）与 arraycopy 的元素级检查时机
    static void testTriggerDeep() {
        Object[] arr = new String[1];
        try {
            arr[0] = Integer.valueOf(1);
            check(false, "storing Integer into String[] must throw");
        } catch (ArrayStoreException e) {
            check(e.getClass() == ArrayStoreException.class, "store throws exact type");
            check("java.lang.Integer".equals(e.getMessage()), "store message is the value class name: " + e.getMessage());
            check("java.lang.ArrayStoreException: java.lang.Integer".equals(e.toString()), "store toString exact");
        }
        arr[0] = null; // null 可存入任意引用数组（对照）
        check(arr[0] == null, "contrast: null is storable into String[]");
        // arraycopy 元素级拷贝时机：第 0 个已拷入、失败元素及其后未被触碰
        Object[] src = new Object[] { "a", Integer.valueOf(2), "c" };
        String[] dst = new String[3];
        try {
            System.arraycopy(src, 0, dst, 0, 3);
            check(false, "arraycopy with an incompatible element must throw");
        } catch (ArrayStoreException e) {
            check(e.getClass() == ArrayStoreException.class, "arraycopy throws exact type");
            if (MODERN) {
                check(e.getMessage() != null && e.getMessage().startsWith("arraycopy: element type mismatch"),
                      "27 arraycopy message shape: " + e.getMessage());
            } else {
                check(e.getMessage() == null, "8 arraycopy message null (measured)");
            }
        }
        check("a".equals(dst[0]), "element 0 already copied when failure hit element 1 (element-wise check timing)");
        check(dst[1] == null, "failing element 1 not written into destination");
        check(dst[2] == null, "element 2 never reached");
        // 长度 0 的 arraycopy 不做兼容性检查（对照）
        System.arraycopy(src, 0, dst, 0, 0);
        check(true, "contrast: zero-length arraycopy never throws");
        // 基本类型数组间不兼容：同为数组但分量类型无子型关系
        try {
            System.arraycopy(new int[1], 0, new byte[1], 0, 1);
            check(false, "int[] into byte[] must throw");
        } catch (ArrayStoreException e) {
            check(e.getClass() == ArrayStoreException.class, "primitive array mismatch exact type");
            if (MODERN) {
                check(e.getMessage() != null && e.getMessage().startsWith("arraycopy: type mismatch"),
                      "27 primitive-array message shape: " + e.getMessage());
            } else {
                check(e.getMessage() == null, "8 primitive-array message null");
            }
        }
        // 对照：Object[] 中的 Integer 可拷入 Comparable[]（Integer 实现 Comparable，子型兼容）
        Object[] covSrc = new Object[] { Integer.valueOf(5) };
        Comparable<?>[] covDst = new Comparable<?>[1];
        System.arraycopy(covSrc, 0, covDst, 0, 1);
        check(covDst[0] instanceof Integer && ((Integer) covDst[0]).intValue() == 5,
              "contrast: subtype-compatible element copies cleanly, no ASE");
        // ArrayStoreException 与 ClassCastException 是不同机制（层级分离已在 hierarchy 断言）
        check(!((Object) new ArrayStoreException() instanceof ClassCastException), "ASE is not a ClassCastException");
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
