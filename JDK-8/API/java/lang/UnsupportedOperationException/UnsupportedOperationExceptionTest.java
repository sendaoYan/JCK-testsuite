/*
 * @test
 * @summary java.lang.UnsupportedOperationException API conformance: RuntimeException
 *          hierarchy, all four constructors, and a thrown instance from mutating an
 *          unmodifiable collection.
 * @run main UnsupportedOperationExceptionTest
 */

public class UnsupportedOperationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        UnsupportedOperationException e = new UnsupportedOperationException();
        check(e instanceof RuntimeException, "UOE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "UOE is Exception/Throwable");
    }

    static void testConstructors() {
        UnsupportedOperationException e1 = new UnsupportedOperationException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        UnsupportedOperationException e2 = new UnsupportedOperationException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        UnsupportedOperationException cause = new UnsupportedOperationException("cause");
        UnsupportedOperationException e3 = new UnsupportedOperationException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        UnsupportedOperationException e4 = new UnsupportedOperationException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        java.util.List<String> l = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>());
        try {
            l.add("x");
            throw new AssertionError("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(e instanceof UnsupportedOperationException, "unmodifiable list add() throws UOE");
        }
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        RuntimeException rc = new RuntimeException("rc");
        { UnsupportedOperationException e = new UnsupportedOperationException();
          check(e.getMessage() == null, "UnsupportedOperationException () message null");
          check(e.getCause() == null, "UnsupportedOperationException () cause null");
          check("java.lang.UnsupportedOperationException".equals(e.toString()), "UnsupportedOperationException () toString exact");
          check(e.getClass() == UnsupportedOperationException.class, "UnsupportedOperationException () exact class"); }
        { UnsupportedOperationException e = new UnsupportedOperationException("m");
          check("m".equals(e.getMessage()), "UnsupportedOperationException (String) message");
          check(e.getCause() == null, "UnsupportedOperationException (String) cause null");
          check("java.lang.UnsupportedOperationException: m".equals(e.toString()), "UnsupportedOperationException (String) toString exact"); }
        { UnsupportedOperationException e = new UnsupportedOperationException("m", rc);
          check("m".equals(e.getMessage()), "UnsupportedOperationException (String,Throwable) message");
          check(e.getCause() == rc, "UnsupportedOperationException (String,Throwable) cause same ref");
          check("java.lang.UnsupportedOperationException: m".equals(e.toString()), "UnsupportedOperationException (String,Throwable) toString exact"); }
        { UnsupportedOperationException e = new UnsupportedOperationException(rc);
          check(rc.toString().equals(e.getMessage()), "UnsupportedOperationException (Throwable) message == cause.toString()");
          check(e.getCause() == rc, "UnsupportedOperationException (Throwable) cause same ref");
          check(("java.lang.UnsupportedOperationException: " + rc.toString()).equals(e.toString()), "UnsupportedOperationException (Throwable) toString exact"); }
        check(new UnsupportedOperationException((String) null).getMessage() == null, "UnsupportedOperationException (null String) message null");
        check("java.lang.UnsupportedOperationException".equals(new UnsupportedOperationException((String) null).toString()), "UnsupportedOperationException (null String) toString has no colon");
        check(new UnsupportedOperationException("m", null).getCause() == null, "UnsupportedOperationException (String,null) cause null");
        check("m".equals(new UnsupportedOperationException("m", null).getMessage()), "UnsupportedOperationException (String,null) message kept");
        check(new UnsupportedOperationException((Throwable) null).getMessage() == null, "UnsupportedOperationException (null Throwable) message null");
        check(new UnsupportedOperationException((Throwable) null).getCause() == null, "UnsupportedOperationException (null Throwable) cause null");
    }

    static void testThrowableContract() {
        UnsupportedOperationException t = new UnsupportedOperationException("m");
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
        { UnsupportedOperationException s = new UnsupportedOperationException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { UnsupportedOperationException s = new UnsupportedOperationException("s");
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
        check(UnsupportedOperationExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.UnsupportedOperationException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + UnsupportedOperationExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.UnsupportedOperationException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(UnsupportedOperationException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == UnsupportedOperationException.class, "lookup.forClass identity");
        check("java.lang.UnsupportedOperationException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == -1242599979055084673L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        UnsupportedOperationException e = new UnsupportedOperationException("m");
        Object o = e;
        check(o instanceof RuntimeException, "is RuntimeException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof Error), "not an Error");
        check(!(o instanceof ReflectiveOperationException), "outside the reflective family");
        check(!(o instanceof IllegalStateException), "sibling separation: not IllegalStateException");
        check(!(o instanceof IllegalArgumentException), "sibling separation: not IllegalArgumentException");
    }

    // 真触发：不可变/固定大小集合的修改操作抛 UnsupportedOperationException（可选操作契约）
    static void testTriggerDeep() {
        java.util.List<String> backing = new java.util.ArrayList<String>();
        backing.add("a");
        java.util.List<String> un = java.util.Collections.unmodifiableList(backing);
        try { un.add("x"); check(false, "unmodifiableList.add must throw"); }
        catch (UnsupportedOperationException e) {
            check(e.getClass() == UnsupportedOperationException.class, "unmodifiableList.add exact type");
            check(e.getMessage() == null, "unmodifiableList.add message null (measured both JDKs)");
        }
        try { un.remove(0); check(false, "unmodifiableList.remove(int) must throw"); }
        catch (UnsupportedOperationException e) {
            check(e.getClass() == UnsupportedOperationException.class, "unmodifiableList.remove exact type");
            check(e.getMessage() == null, "unmodifiableList.remove message null");
        }
        try { un.set(0, "x"); check(false, "unmodifiableList.set must throw"); }
        catch (UnsupportedOperationException e) {
            check(e.getClass() == UnsupportedOperationException.class, "unmodifiableList.set exact type");
            check(e.getMessage() == null, "unmodifiableList.set message null");
        }
        try { un.iterator().remove(); check(false, "unmodifiableList.iterator().remove must throw"); }
        catch (UnsupportedOperationException e) {
            check(e.getClass() == UnsupportedOperationException.class, "unmodifiable iterator.remove exact type");
            check(e.getMessage() == null, "unmodifiable iterator.remove message null");
        }
        try { java.util.Collections.unmodifiableCollection(backing).add("x"); check(false, "unmodifiableCollection.add must throw"); }
        catch (UnsupportedOperationException e) { check(e.getClass() == UnsupportedOperationException.class, "unmodifiableCollection.add exact type"); }
        // Arrays.asList 是固定大小视图：remove 抛 UOE，但 set 是可选操作不抛
        java.util.List<String> fixed = java.util.Arrays.asList("a");
        try { fixed.remove(0); check(false, "Arrays.asList.remove must throw"); }
        catch (UnsupportedOperationException e) {
            check(e.getClass() == UnsupportedOperationException.class, "Arrays.asList.remove exact type");
            check(e.getMessage() == null, "Arrays.asList.remove message null");
        }
        fixed.set(0, "x");
        check("x".equals(fixed.get(0)), "contrast: Arrays.asList.set is supported, no UOE");
        // 对照：可变集合支持全部操作，不抛
        java.util.List<String> mut = new java.util.ArrayList<String>();
        mut.add("a"); mut.set(0, "b"); mut.remove(0);
        check(mut.isEmpty(), "contrast: ArrayList mutates freely, no UOE");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        // lcO2 契约深化：构造器/Throwable 契约/序列化/层级/真触发
        testCtorExact();
        testThrowableContract();
        testSerialIdentity();
        testHierarchyExact();
        testTriggerDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
