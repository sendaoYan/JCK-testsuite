/*
 * @test
 * @summary java.lang.Exception API conformance: checked-exception hierarchy (Exception ->
 *          Throwable, not RuntimeException), all four constructors, and message/cause
 *          propagation including the (Throwable) constructor's message == cause.toString().
 * @run main ExceptionTest
 */

public class ExceptionTest {
    static int passed;

    // Exposes the protected (String, Throwable, boolean, boolean) constructor.
    static class SuppressionException extends Exception {
        SuppressionException(String m, Throwable c, boolean enableSuppression, boolean writableStackTrace) {
            super(m, c, enableSuppression, writableStackTrace);
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Exception");
        check(c.getSuperclass() == Throwable.class, "Exception extends Throwable");
        check(!c.isInterface(), "Exception is a class");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four constructors present");
    }

    static void testHierarchy() {
        Exception e = new Exception();
        check(e instanceof Throwable, "Exception is a Throwable");
        check(!(e instanceof RuntimeException), "Exception is not a RuntimeException (checked)");
    }

    static void testConstructors() {
        Exception e1 = new Exception();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        Exception e2 = new Exception("msg");
        check("msg".equals(e2.getMessage()) && e2.getCause() == null, "(String) ctor");
        Exception cause = new Exception("cause");
        Exception e3 = new Exception("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        Exception e4 = new Exception(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()),
              "(Throwable) ctor message == cause.toString()");
    }

    // ---- Part 2b: protected (String, Throwable, boolean, boolean) ctor ----
    static void testBlackBox() {
        Exception cause = new Exception("cause");

        SuppressionException e = new SuppressionException("m", cause, false, false);
        check("m".equals(e.getMessage()) && e.getCause() == cause, "4-arg ctor sets message and cause");
        e.addSuppressed(new Exception("s"));
        check(e.getSuppressed().length == 0, "enableSuppression=false -> addSuppressed is a no-op");
        e.setStackTrace(new StackTraceElement[]{ new StackTraceElement("C", "m", "F.java", 1) });
        check(e.getStackTrace().length == 0, "writableStackTrace=false -> stack trace stays empty");

        SuppressionException e2 = new SuppressionException("m2", cause, true, true);
        e2.addSuppressed(new Exception("s2"));
        check(e2.getSuppressed().length == 1, "enableSuppression=true -> addSuppressed records");
        check(e2.getStackTrace().length > 0, "writableStackTrace=true -> stack trace filled");
    }


    // ---- Deepening (lcO1, 2026-11): family contract matrix ----
    static void testCtorMatrixFull() {
        Exception cause = new Exception("cause");
        Exception none = new Exception();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.Exception"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        Exception withMsg = new Exception("m");
        check(withMsg.getCause() == null, "(String): cause null");
        check(withMsg.toString().equals("java.lang.Exception: m"), "(String): toString exact");
        check(withMsg.getLocalizedMessage().equals("m"), "(String): localized equals message");
        check(withMsg.getStackTrace().length > 0, "(String): stack trace captured");
        Exception withBoth = new Exception("m", cause);
        check(withBoth.getCause() == cause, "(String,Throwable): cause identity");
        check(withBoth.getMessage().equals("m"), "(String,Throwable): message kept");
        Exception fromCause = new Exception(cause);
        check(fromCause.getCause() == cause, "(Throwable): cause identity");
        check(fromCause.getMessage() != null && fromCause.getMessage().equals(cause.toString()),
              "(Throwable): message is cause.toString()");
        check(new Exception((String) null).getMessage() == null, "(String)null: message null");
        check(new Exception((String) null).toString().equals("java.lang.Exception"), "(String)null: bare toString");
        check(new Exception((Throwable) null).getCause() == null && new Exception((Throwable) null).getMessage() == null,
              "(Throwable)null: null cause and message");
        check(new Exception("m", (Throwable) null).getCause() == null, "(m,null): cause null");
        check(new Exception().getSuppressed().length == 0, "fresh suppressed empty");
    }

    static void testCauseContract() {
        Exception t = new Exception("m");
        check(t.getCause() == null, "cause starts null");
        Exception c = new Exception("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new Exception("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        Exception self = new Exception();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        Exception nullOk = new Exception();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        Exception chained = new Exception("outer", new Exception("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
    }

    static void testSuppressedContract() {
        Exception t = new Exception("m");
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
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContract() {
        Exception t = new Exception("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("ExceptionTest"), "top frame class exact");
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
        Exception t = new Exception("boom");
        t.initCause(new Exception("root"));
        t.addSuppressed(new Exception("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.Exception: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.Exception: sup"), "suppressed section present");
        check(out.contains("Caused by: java.lang.Exception: root"), "cause section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.Exception: boom"),
              "PrintWriter overload works");
    }

    static void testStructureContract() {
        check(Exception.class.getSuperclass() == Throwable.class, "direct superclass exact");
        check(!Exception.class.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(Exception.class.getModifiers()), "instantiable, not abstract");
        check(java.io.Serializable.class.isAssignableFrom(Exception.class), "Serializable");
        try {
            check(Exception.class.getDeclaredConstructor(String.class, Throwable.class, boolean.class, boolean.class) != null,
                  "protected (String,Throwable,boolean,boolean) constructor declared");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Exception e = new Exception();
        check(e instanceof Throwable, "Exception is a Throwable");
        check(!(e instanceof RuntimeException), "Exception is not a RuntimeException (checked)");
        check(!Error.class.isAssignableFrom(e.getClass()), "Exception is not an Error");
    }

    static void testSerializationContract() {
        check(java.io.ObjectStreamClass.lookup(Exception.class).getSerialVersionUID() == -3387516993124229948L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            Exception t = new Exception("ser");
            t.initCause(new Exception("root"));
            t.addSuppressed(new Exception("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            Exception copy = (Exception) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.Exception: ser"), "round-trip toString exact");
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
