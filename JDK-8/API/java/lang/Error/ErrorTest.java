/*
 * @test
 * @summary java.lang.Error API conformance: hierarchy (Error -> Throwable, not Exception),
 *          all four constructors, and message/cause propagation.
 * @run main ErrorTest
 */

public class ErrorTest {
    static int passed;

    // Exposes the protected (String, Throwable, boolean, boolean) constructor.
    static class SuppressionError extends Error {
        SuppressionError(String m, Throwable c, boolean enableSuppression, boolean writableStackTrace) {
            super(m, c, enableSuppression, writableStackTrace);
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Error");
        check(c.getSuperclass() == Throwable.class, "Error extends Throwable");
        check(!c.isInterface(), "Error is a class");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four constructors present");
    }

    static void testHierarchy() {
        Throwable t = new Error();
        check(t instanceof Error, "Error is a Throwable");
        check(!(t instanceof Exception), "Error is not an Exception");
    }

    static void testConstructors() {
        Error e1 = new Error();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        Error e2 = new Error("msg");
        check("msg".equals(e2.getMessage()) && e2.getCause() == null, "(String) ctor");
        Error cause = new Error("cause");
        Error e3 = new Error("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        Error e4 = new Error(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()),
              "(Throwable) ctor message == cause.toString()");
    }

    // ---- Part 2b: protected (String, Throwable, boolean, boolean) ctor ----
    static void testBlackBox() {
        Exception cause = new Exception("cause");

        SuppressionError e = new SuppressionError("m", cause, false, false);
        check("m".equals(e.getMessage()) && e.getCause() == cause, "4-arg ctor sets message and cause");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 0, "enableSuppression=false -> addSuppressed is a no-op");
        e.setStackTrace(new StackTraceElement[]{ new StackTraceElement("C", "m", "F.java", 1) });
        check(e.getStackTrace().length == 0, "writableStackTrace=false -> stack trace stays empty");

        SuppressionError e2 = new SuppressionError("m2", cause, true, true);
        e2.addSuppressed(new Exception("s2"));
        check(e2.getSuppressed().length == 1, "enableSuppression=true -> addSuppressed records");
        check(e2.getStackTrace().length > 0, "writableStackTrace=true -> stack trace filled");
    }


    // ---- Deepening (lcO1, 2026-11): family contract matrix ----
    static void testCtorMatrixFull() {
        Error cause = new Error("cause");
        Error none = new Error();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.Error"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        Error withMsg = new Error("m");
        check(withMsg.getCause() == null, "(String): cause null");
        check(withMsg.toString().equals("java.lang.Error: m"), "(String): toString exact");
        check(withMsg.getLocalizedMessage().equals("m"), "(String): localized equals message");
        check(withMsg.getStackTrace().length > 0, "(String): stack trace captured");
        Error withBoth = new Error("m", cause);
        check(withBoth.getCause() == cause, "(String,Throwable): cause identity");
        check(withBoth.getMessage().equals("m"), "(String,Throwable): message kept");
        Error fromCause = new Error(cause);
        check(fromCause.getCause() == cause, "(Throwable): cause identity");
        check(fromCause.getMessage() != null && fromCause.getMessage().equals(cause.toString()),
              "(Throwable): message is cause.toString()");
        check(new Error((String) null).getMessage() == null, "(String)null: message null");
        check(new Error((String) null).toString().equals("java.lang.Error"), "(String)null: bare toString");
        check(new Error((Throwable) null).getCause() == null && new Error((Throwable) null).getMessage() == null,
              "(Throwable)null: null cause and message");
        check(new Error("m", (Throwable) null).getCause() == null, "(m,null): cause null");
        check(new Error().getSuppressed().length == 0, "fresh suppressed empty");
    }

    static void testCauseContract() {
        Error t = new Error("m");
        check(t.getCause() == null, "cause starts null");
        Error c = new Error("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new Error("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        Error self = new Error();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        Error nullOk = new Error();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        Error chained = new Error("outer", new Error("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
    }

    static void testSuppressedContract() {
        Error t = new Error("m");
        check(t.getSuppressed().length == 0, "suppressed empty by default");
        try {
            t.addSuppressed(null);
            throw new AssertionError("unreachable: addSuppressed(null)");
        } catch (NullPointerException e) {
            check("Cannot suppress a null exception.".equals(e.getMessage()), "addSuppressed(null) NPE message exact");
        }
        try {
            t.addSuppressed(t);
            throw new AssertionError("unreachable: addSuppressed(self)");
        } catch (IllegalArgumentException e) {
            check("Self-suppression not permitted".equals(e.getMessage()), "addSuppressed(self) IAE message exact");
        }
        Error s1 = new Error("s1");
        Error s2 = new Error("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContract() {
        Error t = new Error("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("ErrorTest"), "top frame class exact");
        check(st[0].getMethodName().equals("testStackTraceContract"), "top frame method exact");
        StackTraceElement[] copy = t.getStackTrace();
        copy[0] = null;
        check(t.getStackTrace()[0] != null, "getStackTrace independent copy");
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("C", "m", "F.java", 1) });
        check(t.getStackTrace().length == 1 && t.getStackTrace()[0].getClassName().equals("C"), "setStackTrace roundtrip");
        t.setStackTrace(new StackTraceElement[0]);
        check(t.getStackTrace().length == 0, "setStackTrace(empty) clears");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        check(t.getStackTrace().length > 0, "fillInStackTrace refills after clear");
    }

    static void testPrintStackTraceContract() {
        Error t = new Error("boom");
        t.initCause(new Error("root"));
        t.addSuppressed(new Error("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.Error: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.Error: sup"), "suppressed section present");
        check(out.contains("Caused by: java.lang.Error: root"), "cause section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.Error: boom"),
              "PrintWriter overload works");
    }

    static void testStructureContract() {
        check(Error.class.getSuperclass() == Throwable.class, "direct superclass exact");
        check(!Error.class.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(Error.class.getModifiers()), "instantiable, not abstract");
        check(java.io.Serializable.class.isAssignableFrom(Error.class), "Serializable");
        try {
            check(Error.class.getDeclaredConstructor(String.class, Throwable.class, boolean.class, boolean.class) != null,
                  "protected (String,Throwable,boolean,boolean) constructor declared");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Error e = new Error();
        check(e instanceof Throwable, "Error is a Throwable");
        check(!Exception.class.isAssignableFrom(e.getClass()), "Error is not an Exception");
        check(!RuntimeException.class.isAssignableFrom(e.getClass()), "Error is not a RuntimeException");
    }

    static void testSerializationContract() {
        check(java.io.ObjectStreamClass.lookup(Error.class).getSerialVersionUID() == 4980196508277280342L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            Error t = new Error("ser");
            t.initCause(new Error("root"));
            t.addSuppressed(new Error("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            Error copy = (Error) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.Error: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testHierarchy();
        testConstructors();
        testBlackBox();
        testCtorMatrixFull();
        testCauseContract();
        testSuppressedContract();
        testStackTraceContract();
        testPrintStackTraceContract();
        testStructureContract();
        testSerializationContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
