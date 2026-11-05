/*
 * @test
 * @summary java.lang.IllegalStateException API conformance: RuntimeException hierarchy,
 *          all four constructors, and a thrown instance (Iterator.remove before next()).
 * @run main IllegalStateExceptionTest
 */

public class IllegalStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        IllegalStateException e = new IllegalStateException();
        check(e instanceof RuntimeException, "ISE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "ISE is Exception/Throwable");
    }

    static void testConstructors() {
        IllegalStateException e1 = new IllegalStateException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        IllegalStateException e2 = new IllegalStateException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        IllegalStateException cause = new IllegalStateException("cause");
        IllegalStateException e3 = new IllegalStateException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        IllegalStateException e4 = new IllegalStateException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        java.util.Iterator<String> it = new java.util.ArrayList<String>().iterator();
        try {
            it.remove();
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException e) {
            check(e instanceof IllegalStateException, "Iterator.remove before next() throws ISE");
        }
    }


    // ===================== 契约深化（2026-11，lcO2 追加，只增不删） =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorExact() {
        RuntimeException rc = new RuntimeException("rc");
        { IllegalStateException e = new IllegalStateException();
          check(e.getMessage() == null, "IllegalStateException () message null");
          check(e.getCause() == null, "IllegalStateException () cause null");
          check("java.lang.IllegalStateException".equals(e.toString()), "IllegalStateException () toString exact");
          check(e.getClass() == IllegalStateException.class, "IllegalStateException () exact class"); }
        { IllegalStateException e = new IllegalStateException("m");
          check("m".equals(e.getMessage()), "IllegalStateException (String) message");
          check(e.getCause() == null, "IllegalStateException (String) cause null");
          check("java.lang.IllegalStateException: m".equals(e.toString()), "IllegalStateException (String) toString exact"); }
        { IllegalStateException e = new IllegalStateException("m", rc);
          check("m".equals(e.getMessage()), "IllegalStateException (String,Throwable) message");
          check(e.getCause() == rc, "IllegalStateException (String,Throwable) cause same ref");
          check("java.lang.IllegalStateException: m".equals(e.toString()), "IllegalStateException (String,Throwable) toString exact"); }
        { IllegalStateException e = new IllegalStateException(rc);
          check(rc.toString().equals(e.getMessage()), "IllegalStateException (Throwable) message == cause.toString()");
          check(e.getCause() == rc, "IllegalStateException (Throwable) cause same ref");
          check(("java.lang.IllegalStateException: " + rc.toString()).equals(e.toString()), "IllegalStateException (Throwable) toString exact"); }
        check(new IllegalStateException((String) null).getMessage() == null, "IllegalStateException (null String) message null");
        check("java.lang.IllegalStateException".equals(new IllegalStateException((String) null).toString()), "IllegalStateException (null String) toString has no colon");
        check(new IllegalStateException("m", null).getCause() == null, "IllegalStateException (String,null) cause null");
        check("m".equals(new IllegalStateException("m", null).getMessage()), "IllegalStateException (String,null) message kept");
        check(new IllegalStateException((Throwable) null).getMessage() == null, "IllegalStateException (null Throwable) message null");
        check(new IllegalStateException((Throwable) null).getCause() == null, "IllegalStateException (null Throwable) cause null");
    }

    static void testThrowableContract() {
        IllegalStateException t = new IllegalStateException("m");
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
        { IllegalStateException s = new IllegalStateException("s");
          try { s.initCause(s); check(false, "initCause(self) must throw IllegalArgumentException"); }
          catch (IllegalArgumentException e) { check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE exact message"); } }
        { IllegalStateException s = new IllegalStateException("s");
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
        check(IllegalStateExceptionTest.class.getName().equals(t.getStackTrace()[0].getClassName()), "top frame is this test class");
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
        check(dump.startsWith("java.lang.IllegalStateException: m"), "printStackTrace first line exact");
        check(dump.contains("\tat " + IllegalStateExceptionTest.class.getName() + ".testThrowableContract"),
              "printStackTrace contains the real calling frame");
        check(dump.contains("Suppressed: java.lang.Exception: s1"), "printStackTrace suppressed section s1");
        check(dump.contains("Suppressed: java.lang.Exception: s2"), "printStackTrace suppressed section s2");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        check(sw.toString().startsWith("java.lang.IllegalStateException: m"), "printStackTrace(PrintWriter) first line exact");
        check("m".equals(t.getLocalizedMessage()), "getLocalizedMessage == getMessage");
    }

    static void testSerialIdentity() {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(IllegalStateException.class);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.forClass() == IllegalStateException.class, "lookup.forClass identity");
        check("java.lang.IllegalStateException".equals(osc.getName()), "stream class name exact");
        check(osc.getSerialVersionUID() == -1848914673093119416L, "serialVersionUID exact (identical on JDK-8 and JDK-27)");
    }

    static void testHierarchyExact() {

        IllegalStateException e = new IllegalStateException("m");
        Object o = e;
        check(o instanceof RuntimeException, "is RuntimeException");
        check(o instanceof Exception, "is Exception");
        check(o instanceof Throwable, "is Throwable");
        check(o instanceof java.io.Serializable, "is Serializable");
        check(!(o instanceof Error), "not an Error");
        check(!(o instanceof ReflectiveOperationException), "outside the reflective family");
        check(!(o instanceof UnsupportedOperationException), "sibling separation: not UnsupportedOperationException");
        check(!(o instanceof IllegalArgumentException), "sibling separation: not IllegalArgumentException");
    }

    // 真触发：Iterator.remove 未先 next() 抛 IllegalStateException（lang 视角锁定类型+消息实测）
    static void testTriggerDeep() throws Exception {
        java.util.List<String> l = new java.util.ArrayList<String>();
        l.add("a");
        java.util.Iterator<String> it = l.iterator();
        try {
            it.remove();
            check(false, "iterator.remove before next() must throw");
        } catch (IllegalStateException e) {
            check(e.getClass() == IllegalStateException.class, "ArrayList iterator.remove throws exact type");
            check(e.getMessage() == null, "triggered ISE message is null on both JDKs (measured)");
            check(e.getCause() == null, "triggered ISE cause null");
        }
        // next() 之后 remove() 合法，随后再 remove()（中间无 next()）又抛
        java.util.Iterator<String> it2 = l.iterator();
        it2.next();
        it2.remove();
        check(l.isEmpty(), "legit iterator.remove actually removed the element");
        try {
            it2.remove();
            check(false, "second consecutive remove must throw");
        } catch (IllegalStateException e) {
            check(e.getClass() == IllegalStateException.class, "second remove exact type");
            check(e.getMessage() == null, "second remove message null");
        }
        // 同一契约在 LinkedList 迭代器上一致
        java.util.List<String> ll = new java.util.LinkedList<String>();
        ll.add("x");
        try {
            ll.iterator().remove();
            check(false, "LinkedList iterator.remove before next() must throw");
        } catch (IllegalStateException e) {
            check(e.getClass() == IllegalStateException.class, "LinkedList iterator path exact type");
        }
        // 对照：ByteArrayOutputStream.close() 后写不抛 ISE（close 对它无监控语义）
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        bos.close();
        bos.write(65);
        check(bos.size() == 1, "contrast: BAOS stays writable after close (no ISE)");
    }

    public static void main(String[] args) throws Exception {
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
