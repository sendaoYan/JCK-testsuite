/*
 * @test
 * @summary java.lang.SecurityException conformance: white-box structure + black-box behavior.
 * @run main SecurityExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SecurityExceptionTest {
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
        Class<?> c = Class.forName("java.lang.SecurityException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.SecurityException");

        SecurityException e0 = new SecurityException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { SecurityException e = new SecurityException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); SecurityException e = new SecurityException("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }
        { Exception cause = new Exception("cause"); SecurityException e = new SecurityException(cause); check(e.getCause() == cause, "(Throwable) ctor cause"); }

        // common Throwable contract
        SecurityException t = new SecurityException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.SecurityException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ================= 契约深化（2026-11）追加 =================
    static void testConstructorEdges() {
        check("".equals(new SecurityException("").getMessage()), "empty String message preserved");
        // (Throwable) with null: message null, cause null, but the cause slot
        // is considered initialized so a later initCause is rejected.
        SecurityException nullT = new SecurityException((Throwable) null);
        check(nullT.getMessage() == null, "(null Throwable) message null");
        check(nullT.getCause() == null, "(null Throwable) cause null");
        try {
            nullT.initCause(new Exception("later"));
            throw new AssertionError("initCause after (Throwable)null: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        SecurityException strNull = new SecurityException("m", null);
        check("m".equals(strNull.getMessage()), "(String, null) message kept");
        check(strNull.getCause() == null, "(String, null) cause null");
        Exception c = new Exception("c");
        SecurityException nullStr = new SecurityException(null, c);
        check(nullStr.getMessage() == null, "(null, Throwable) message null");
        check(nullStr.getCause() == c, "(null, Throwable) cause kept");
        check(new SecurityException(c).getMessage().equals(c.toString()),
              "cause-only message mirrors cause toString");
    }

    static void testCauseSemantics() {
        SecurityException fresh = new SecurityException();
        check(fresh.getCause() == null, "fresh cause null");
        check(fresh.initCause(new Exception("first")) == fresh, "initCause returns this");
        try {
            fresh.initCause(new Exception("second"));
            throw new AssertionError("second initCause: expected IllegalStateException");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with"),
                  "second initCause message prefix [JDK27]");
        }
        try {
            SecurityException s = new SecurityException();
            s.initCause(s);
            throw new AssertionError("self cause: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()),
                  "self-causation exact message [JDK27]");
        }
        SecurityException fixed = new SecurityException("m", new Exception("ctor-cause"));
        try {
            fixed.initCause(new Exception("other"));
            throw new AssertionError("ctor cause overwrite: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        SecurityException cleared = new SecurityException();
        cleared.initCause(null);
        check(cleared.getCause() == null, "initCause(null) leaves cause null");
        SecurityException setTwice = new SecurityException();
        setTwice.initCause(new Exception("x"));
        try {
            setTwice.initCause(null);
            throw new AssertionError("initCause(null) after set: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    static void testSuppressedDeep() {
        SecurityException e = new SecurityException();
        check(e.getSuppressed().length == 0, "no suppressed by default");
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2, "two suppressed recorded");
        check(e.getSuppressed()[0] == s1 && e.getSuppressed()[1] == s2,
              "suppressed order and identity");
        Throwable[] view = e.getSuppressed();
        view[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed defensive copy");
        try {
            e.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NPE");
        } catch (NullPointerException npe) {
            check("Cannot suppress a null exception.".equals(npe.getMessage()),
                  "addSuppressed(null) exact message [JDK27]");
        }
    }

    static void testStackTraceHandling() {
        SecurityException e = new SecurityException("m");
        check(e.getStackTrace().length >= 1, "stack trace captured");
        check(e.getStackTrace()[0].getClassName().equals("SecurityExceptionTest"),
              "top frame is this class");
        SecurityException refilled = (SecurityException) e.fillInStackTrace();
        check(refilled == e, "fillInStackTrace returns this");
        check(refilled.getStackTrace().length >= 1, "refilled trace non-empty");
        StackTraceElement[] arr = e.getStackTrace();
        arr[0] = null;
        check(e.getStackTrace()[0] != null, "getStackTrace defensive copy");
        StackTraceElement[] set = new StackTraceElement[] {
            new StackTraceElement("FooClass", "fooMethod", "FooClass.java", 42),
            new StackTraceElement("BarClass", "barMethod", "BarClass.java", -1),
        };
        e.setStackTrace(set);
        StackTraceElement[] back = e.getStackTrace();
        check(back.length == 2, "setStackTrace length");
        check(back[0].getClassName().equals("FooClass"), "element 0 class name");
        check(back[0].getMethodName().equals("fooMethod"), "element 0 method name");
        check(back[0].getFileName().equals("FooClass.java"), "element 0 file name");
        check(back[0].getLineNumber() == 42, "element 0 line number");
        check(!back[0].isNativeMethod(), "element 0 not native");
        check(back[1].getLineNumber() == -1, "element 1 unknown line");
        check(back[1].toString().equals("BarClass.barMethod(BarClass.java)"),
              "element toString exact for unknown line");
        try {
            e.setStackTrace(null);
            throw new AssertionError("setStackTrace(null): expected NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            e.setStackTrace(new StackTraceElement[]{null});
            throw new AssertionError("setStackTrace({null}): expected NPE");
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null && npe.getMessage().startsWith("stackTrace[0]"),
                  "setStackTrace({null}) message [JDK27]");
        }
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) legal");
    }

    static void testToStringExactAndPrintWriter() throws Exception {
        check("java.lang.SecurityException".equals(new SecurityException().toString()),
              "toString no message [JDK27]");
        check("java.lang.SecurityException: boom".equals(new SecurityException("boom").toString()),
              "toString with message [JDK27]");
        SecurityException e = new SecurityException("print-me");
        e.addSuppressed(new Exception("sup-me"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintWriter(new java.io.OutputStreamWriter(bos, "UTF-8"), true));
        String printed = bos.toString("UTF-8");
        check(printed.contains("java.lang.SecurityException: print-me"), "printStackTrace(PrintWriter) header");
        check(printed.contains("Suppressed: java.lang.Exception: sup-me"),
              "printStackTrace Suppressed section");
        check(printed.contains("testToStringExactAndPrintWriter"), "printStackTrace frame present");
    }

    static void testSerialization() throws Exception {
        check(new SecurityException() instanceof java.io.Serializable, "Serializable");
        long svuid = java.io.ObjectStreamClass.lookup(SecurityException.class).getSerialVersionUID();
        check(svuid == 6878364983674394167L, "serialVersionUID locked 6878364983674394167");
        SecurityException e = new SecurityException("ser-msg", new Exception("ser-cause"));
        e.setStackTrace(new StackTraceElement[]{new StackTraceElement("S", "t", "S.java", 1)});
        e.addSuppressed(new Exception("ser-sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        SecurityException back = (SecurityException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof SecurityException, "round-trip type");
        check("ser-msg".equals(back.getMessage()), "round-trip message");
        check("ser-cause".equals(back.getCause().getMessage()), "round-trip cause");
        check(back.getStackTrace().length == 1, "round-trip stack trace length");
        check(back.getStackTrace()[0].getMethodName().equals("t"), "round-trip stack trace content");
        check(back.getSuppressed().length == 1, "round-trip suppressed length");
        check("ser-sup".equals(back.getSuppressed()[0].getMessage()), "round-trip suppressed content");
        check(back.toString().startsWith("java.lang.SecurityException"), "round-trip toString prefix");
    }

    static void testWhiteBoxExtra() throws Exception {
        Class<?> c = Class.forName("java.lang.SecurityException");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) ctor");
        check(c.getConstructor(Throwable.class) != null, "(Throwable) ctor");
        check(RuntimeException.class.isAssignableFrom(c), "RuntimeException assignable");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable assignable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorEdges();
        testCauseSemantics();
        testSuppressedDeep();
        testStackTraceHandling();
        testToStringExactAndPrintWriter();
        testSerialization();
        testWhiteBoxExtra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
