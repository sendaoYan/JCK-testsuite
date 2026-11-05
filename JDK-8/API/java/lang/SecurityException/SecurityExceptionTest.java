/*
 * @test
 * @summary java.lang.SecurityException API conformance: RuntimeException hierarchy and all
 *          four constructors.
 * @run main SecurityExceptionTest
 */

public class SecurityExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        SecurityException e = new SecurityException();
        check(e instanceof RuntimeException, "SecurityException is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable,
              "SecurityException is Exception/Throwable");
    }

    static void testConstructors() {
        SecurityException e1 = new SecurityException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        SecurityException e2 = new SecurityException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        SecurityException cause = new SecurityException("cause");
        SecurityException e3 = new SecurityException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        SecurityException e4 = new SecurityException(cause);
        check(e4.getCause() == cause, "(Throwable) ctor sets cause");
    }

    // ================= 契约深化（2026-11）追加 =================
    static void testConstructorEdges() {
        check("".equals(new SecurityException("").getMessage()), "empty String message preserved");
        // (Throwable) with null: no message, no cause; the cause slot is
        // still considered initialized, so a later initCause is rejected.
        SecurityException nullT = new SecurityException((Throwable) null);
        check(nullT.getMessage() == null, "(null Throwable) message null");
        check(nullT.getCause() == null, "(null Throwable) cause null");
        try {
            nullT.initCause(new Exception("later"));
            throw new AssertionError("initCause after (Throwable)null ctor: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // (String, null): message kept, cause null.
        SecurityException strNull = new SecurityException("m", null);
        check("m".equals(strNull.getMessage()), "(String, null) message kept");
        check(strNull.getCause() == null, "(String, null) cause null");
        // (null, Throwable): message null but cause recorded.
        Exception c = new Exception("c");
        SecurityException nullStr = new SecurityException(null, c);
        check(nullStr.getMessage() == null, "(null, Throwable) message null");
        check(nullStr.getCause() == c, "(null, Throwable) cause kept");
        // Constructor-message from a cause-only exception mirrors the cause.
        SecurityException fromCause = new SecurityException(c);
        check(fromCause.getMessage().equals(c.toString()), "cause-only message mirrors cause toString");
    }

    static void testCauseSemantics() {
        SecurityException fresh = new SecurityException();
        check(fresh.getCause() == null, "fresh cause null");
        Exception first = new Exception("first");
        check(fresh.initCause(first) == fresh, "initCause returns this");
        check(fresh.getCause() == first, "initCause sets cause");
        try {
            fresh.initCause(new Exception("second"));
            throw new AssertionError("second initCause: expected IllegalStateException");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Can't overwrite cause with"),
                  "second initCause message prefix [JDK8]");
        }
        // Self-causation is rejected outright.
        try {
            SecurityException s = new SecurityException();
            s.initCause(s);
            throw new AssertionError("self cause: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()),
                  "self-causation exact message [JDK8]");
        }
        // Constructor-supplied cause cannot be overwritten either.
        SecurityException fixed = new SecurityException("m", new Exception("ctor-cause"));
        try {
            fixed.initCause(new Exception("other"));
            throw new AssertionError("ctor cause overwrite: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // initCause(null) on a fresh exception is legal and leaves no cause.
        SecurityException cleared = new SecurityException();
        cleared.initCause(null);
        check(cleared.getCause() == null, "initCause(null) leaves cause null");
        try {
            cleared.initCause(new Exception("x"));
            throw new AssertionError("initCause after initCause(null): expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // initCause(null) on an already-set cause is an overwrite attempt too.
        SecurityException setTwice = new SecurityException();
        setTwice.initCause(new Exception("x"));
        try {
            setTwice.initCause(null);
            throw new AssertionError("initCause(null) after set: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    static void testSuppressed() {
        SecurityException e = new SecurityException();
        check(e.getSuppressed().length == 0, "no suppressed by default");
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2, "two suppressed recorded");
        check(e.getSuppressed()[0] == s1 && e.getSuppressed()[1] == s2,
              "suppressed order preserved, identities kept");
        // Defensive copy: mutating the returned array changes nothing.
        Throwable[] view = e.getSuppressed();
        view[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed defensive copy");
        try {
            e.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NPE");
        } catch (NullPointerException npe) {
            check("Cannot suppress a null exception.".equals(npe.getMessage()),
                  "addSuppressed(null) exact message [JDK8]");
        }
    }

    static void testStackTraceHandling() {
        SecurityException e = new SecurityException("m");
        check(e.getStackTrace().length >= 1, "stack trace captured");
        check(e.getStackTrace()[0].getClassName().equals("SecurityExceptionTest"),
              "top frame is this class");
        // fillInStackTrace returns this and refreshes the trace.
        SecurityException refilled = (SecurityException) e.fillInStackTrace();
        check(refilled == e, "fillInStackTrace returns this");
        check(refilled.getStackTrace().length >= 1, "refilled trace non-empty");
        // Defensive copy of getStackTrace.
        StackTraceElement[] arr = e.getStackTrace();
        arr[0] = null;
        check(e.getStackTrace()[0] != null, "getStackTrace defensive copy");
        // setStackTrace round-trip with exact element fields.
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
        check(back[0].isNativeMethod() == false, "element 0 not native");
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
                  "setStackTrace({null}) message [JDK8]");
        }
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) legal");
    }

    static void testToStringAndPrinting() throws Exception {
        check("java.lang.SecurityException".equals(new SecurityException().toString()),
              "toString no message [JDK8]");
        check("java.lang.SecurityException: boom".equals(new SecurityException("boom").toString()),
              "toString with message [JDK8]");
        SecurityException e = new SecurityException("print-me");
        check(e.getLocalizedMessage().equals("print-me"), "getLocalizedMessage == getMessage");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String printed = bos.toString("UTF-8");
        check(printed.contains("java.lang.SecurityException: print-me"), "printStackTrace(PrintStream) header");
        check(printed.contains("testToStringAndPrinting"), "printStackTrace frame present");
        e.addSuppressed(new Exception("sup-me"));
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintWriter(new java.io.OutputStreamWriter(bos2, "UTF-8"), true));
        String printed2 = bos2.toString("UTF-8");
        check(printed2.contains("java.lang.SecurityException: print-me"), "printStackTrace(PrintWriter) header");
        check(printed2.contains("Suppressed: java.lang.Exception: sup-me"),
              "printStackTrace Suppressed section");
    }

    static void testSerialization() throws Exception {
        check(new SecurityException() instanceof java.io.Serializable, "Serializable");
        long svuid = java.io.ObjectStreamClass.lookup(SecurityException.class).getSerialVersionUID();
        check(svuid == 6878364983674394167L, "serialVersionUID locked 6878364983674394167");
        SecurityException e = new SecurityException("ser-msg",
                new Exception("ser-cause"));
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.SecurityException");
        check(c.getSuperclass() == RuntimeException.class, "superclass RuntimeException");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getDeclaredConstructors().length == 4, "4 declared constructors");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) ctor");
        check(c.getConstructor(Throwable.class) != null, "(Throwable) ctor");
        check(RuntimeException.class.isAssignableFrom(c), "RuntimeException assignable");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable assignable");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testConstructors();
        testConstructorEdges();
        testCauseSemantics();
        testSuppressed();
        testStackTraceHandling();
        testToStringAndPrinting();
        testSerialization();
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
