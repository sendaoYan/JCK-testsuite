/*
 * @test
 * @summary java.lang.ExceptionInInitializerError API conformance: LinkageError hierarchy,
 *          constructors, and a thrown instance wrapping the exception from a failing
 *          static initializer, exposed via getCause()/getException().
 * @run main ExceptionInInitializerErrorTest
 */

public class ExceptionInInitializerErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class BadInit {
        static {
            String s = null;
            s.length(); // throws NullPointerException during <clinit>
        }
    }

    static void testHierarchy() {
        ExceptionInInitializerError e = new ExceptionInInitializerError();
        check(e instanceof LinkageError, "EIIE is a LinkageError");
        check(e instanceof Error, "EIIE is an Error");
    }

    static void testConstructors() {
        ExceptionInInitializerError e1 = new ExceptionInInitializerError();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ExceptionInInitializerError e2 = new ExceptionInInitializerError("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
        Throwable cause = new Throwable("cause");
        ExceptionInInitializerError e3 = new ExceptionInInitializerError(cause);
        check(e3.getCause() == cause, "(Throwable) ctor sets cause");
    }

    static void testThrown() {
        try {
            new BadInit();
            throw new AssertionError("expected ExceptionInInitializerError");
        } catch (ExceptionInInitializerError e) {
            check(e.getCause() instanceof NullPointerException,
                  "cause is the exception from <clinit>");
            check(e.getException() == e.getCause(), "getException returns the cause");
        }
    }

    // ---- Deepening (lcO1, 2026-11): ExceptionInInitializerError closed loops ----
    static class MarkerHolder {
        static final RuntimeException MARKER = new RuntimeException("MARKER");
    }

    static class BadInitRt {
        static {
            if (true) throw MarkerHolder.MARKER;
        }
        static final Object A = new Object();
        static final Object B = new Object();
        static int touch() { return 1; }
    }

    static class BadInitExpr {
        static final int X = 1 / 0;
    }

    static class BadInitErr {
        static {
            if (true) throw new OutOfMemoryError("OOM-MARK");
        }
        static final Object A = new Object();
    }

    static void testThrownRealClosedLoop() {
        RuntimeException marker = MarkerHolder.MARKER; // force marker class init first (harmless)
        try {
            Object o = BadInitRt.A;
            throw new AssertionError("unreachable: first static access must fail");
        } catch (ExceptionInInitializerError e) {
            check(true, "first static access throws ExceptionInInitializerError");
            check(e.getCause() == marker, "getCause() is the exact <clinit> exception instance");
            check(e.getException() == marker, "getException() is the same instance");
            check(e.getException() == e.getCause(), "getException() == getCause()");
            check(e.getMessage() == null, "JVM-generated EIIE message is null");
            check(e instanceof LinkageError, "EIIE is a LinkageError");
            check(e instanceof Error, "EIIE is an Error");
            check(!Exception.class.isAssignableFrom(e.getClass()), "EIIE is not an Exception");
            check(e.getSuppressed().length == 0, "no suppressed on the JVM-thrown EIIE");
            check(e.getStackTrace().length > 0, "EIIE carries its own stack trace");
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            e.printStackTrace(new java.io.PrintStream(bos));
            String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            check(out.startsWith("java.lang.ExceptionInInitializerError"), "printStackTrace first line exact");
            check(out.contains("Caused by: java.lang.RuntimeException: MARKER"), "Caused by names the marker");
        }
    }

    static void testSecondAccessNcdfe() {
        String expected = "Could not initialize class " + BadInitRt.class.getName();
        try {
            Object o = BadInitRt.B;
            throw new AssertionError("unreachable: second access must fail");
        } catch (NoClassDefFoundError e) {
            check(expected.equals(e.getMessage()),
                  "second static access -> NoClassDefFoundError 'Could not initialize class <name>'");
            check(e instanceof LinkageError, "NCDFE is a LinkageError");
        }
        try {
            int v = BadInitRt.touch();
            throw new AssertionError("unreachable: static method call must fail");
        } catch (NoClassDefFoundError e) {
            check(expected.equals(e.getMessage()), "static method call -> same NCDFE message");
        }
        try {
            Object o = new BadInitRt();
            throw new AssertionError("unreachable: instantiation must fail");
        } catch (NoClassDefFoundError e) {
            check(expected.equals(e.getMessage()), "instantiation -> same NCDFE message");
        }
        try {
            Class<?> c = Class.forName(BadInitRt.class.getName());
            throw new AssertionError("unreachable: Class.forName must fail");
        } catch (NoClassDefFoundError e) {
            check(expected.equals(e.getMessage()), "Class.forName -> same NCDFE message");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("unexpected CNFE: " + e);
        }
    }

    static void testStaticExprInit() {
        try {
            int v = BadInitExpr.X;
            throw new AssertionError("unreachable: static expression must fail");
        } catch (ExceptionInInitializerError e) {
            check(e.getCause() instanceof ArithmeticException, "1/0 in static initializer wrapped as AE cause");
            check("/ by zero".equals(e.getCause().getMessage()), "cause message '/ by zero' exact");
            check(e.getException() == e.getCause(), "getException == getCause for expression failure");
        }
        try {
            int v = BadInitExpr.X;
            throw new AssertionError("unreachable: second expression access must fail");
        } catch (NoClassDefFoundError e) {
            check(("Could not initialize class " + BadInitExpr.class.getName()).equals(e.getMessage()),
                  "second access to expression-failed class -> NCDFE with exact message");
        }
    }

    static void testStaticBlockErrorPropagates() {
        try {
            Object o = BadInitErr.A;
            throw new AssertionError("unreachable: Error in <clinit> must propagate");
        } catch (OutOfMemoryError e) {
            check("OOM-MARK".equals(e.getMessage()), "Error from <clinit> propagates unwrapped (JLS 12.4.2)");
            check(!ExceptionInInitializerError.class.isAssignableFrom(e.getClass()), "not wrapped in EIIE");
        }
        try {
            Object o = BadInitErr.A;
            throw new AssertionError("unreachable: second access must fail");
        } catch (NoClassDefFoundError e) {
            check(true, "second access after Error-in-<clinit> -> NCDFE");
        }
    }

    static void testConstructorsFull() {
        ExceptionInInitializerError e0 = new ExceptionInInitializerError();
        check(e0.getMessage() == null, "() message null");
        check(e0.getException() == null, "() exception null");
        check(e0.getCause() == null, "() cause null");
        check(e0.toString().equals("java.lang.ExceptionInInitializerError"), "() toString bare");
        ExceptionInInitializerError em = new ExceptionInInitializerError("msg");
        check("msg".equals(em.getMessage()), "(String) message");
        check(em.getException() == null, "(String) exception null");
        check(em.getCause() == null, "(String) cause null");
        check(em.toString().equals("java.lang.ExceptionInInitializerError: msg"), "(String) toString");
        check(em.getLocalizedMessage().equals("msg"), "(String) localized equals message");
        RuntimeException cause = new RuntimeException("rt");
        ExceptionInInitializerError ec = new ExceptionInInitializerError(cause);
        check(ec.getMessage() == null, "(Throwable) message null");
        check(ec.getException() == cause, "(Throwable) exception identity");
        check(ec.getCause() == cause, "(Throwable) cause identity");
        check(new ExceptionInInitializerError((Throwable) null).getException() == null, "(null Throwable) exception null");
        check(new ExceptionInInitializerError((String) null).getMessage() == null, "(null String) message null");
    }

    static void testInitCauseBlocked() {
        ExceptionInInitializerError[] forms = {
            new ExceptionInInitializerError(),
            new ExceptionInInitializerError("m"),
            new ExceptionInInitializerError(new RuntimeException("c"))
        };
        for (int i = 0; i < forms.length; i++) {
            try {
                forms[i].initCause(new Exception("x"));
                throw new AssertionError("unreachable: initCause must be pinned for form " + i);
            } catch (IllegalStateException e) {
                check(e.getMessage().startsWith("Can't overwrite cause with"),
                      "form " + i + ": initCause pinned with ISE");
            }
        }
    }

    static void testCommonContract() {
        ExceptionInInitializerError t = new ExceptionInInitializerError("m");
        check(t.getSuppressed().length == 0, "suppressed empty by default");
        t.addSuppressed(new Exception("sup"));
        check(t.getSuppressed().length == 1, "addSuppressed records");
        try {
            new ExceptionInInitializerError().addSuppressed(null);
            throw new AssertionError("unreachable");
        } catch (NullPointerException e) {
            check("Cannot suppress a null exception.".equals(e.getMessage()), "addSuppressed(null) NPE exact");
        }
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0 && st[0].getClassName().equals("ExceptionInInitializerErrorTest"),
              "top frame class exact");
        check(st[0].getMethodName().equals("testCommonContract"), "top frame method exact");
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("C", "m", "F.java", 1) });
        check(t.getStackTrace().length == 1, "setStackTrace roundtrip");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8)
              .startsWith("java.lang.ExceptionInInitializerError: m"), "printStackTrace first line");
        check(t.getLocalizedMessage().equals(t.getMessage()), "localized equals message");
    }

    static void testSerializationContract() {
        check(java.io.ObjectStreamClass.lookup(ExceptionInInitializerError.class).getSerialVersionUID()
              == 1521711792217232256L, "serialVersionUID locked via ObjectStreamClass (same on 8 and 27)");
        try {
            ExceptionInInitializerError t = new ExceptionInInitializerError(new RuntimeException("rt"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            ExceptionInInitializerError copy = (ExceptionInInitializerError) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check(copy.getException() != null && "rt".equals(copy.getException().getMessage()),
                  "round-trip keeps the wrapped exception");
            check(copy.getCause() != null && "rt".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.toString().startsWith("java.lang.ExceptionInInitializerError"), "round-trip toString");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void testReflectionAndFamily() {
        check(ExceptionInInitializerError.class.getSuperclass() == LinkageError.class, "superclass LinkageError");
        check(java.io.Serializable.class.isAssignableFrom(ExceptionInInitializerError.class), "Serializable");
        check(!java.lang.reflect.Modifier.isAbstract(ExceptionInInitializerError.class.getModifiers()),
              "instantiable, not abstract");
        try {
            check(ExceptionInInitializerError.class.getConstructor(Throwable.class) != null, "(Throwable) public");
            check(ExceptionInInitializerError.class.getConstructor(String.class) != null, "(String) public");
            ExceptionInInitializerError.class.getConstructor(String.class, Throwable.class);
            throw new AssertionError("unreachable: (String,Throwable) must not exist");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }


    static void testNcdfeDetails() {
        try {
            int v = BadInitRt.touch();
            throw new AssertionError("unreachable");
        } catch (NoClassDefFoundError e) {
            check(e instanceof Error, "NCDFE is an Error");
            check(e instanceof LinkageError, "NCDFE is a LinkageError");
            check(!Exception.class.isAssignableFrom(e.getClass()), "NCDFE is not an Exception");
            check(e.getMessage().startsWith("Could not initialize class "), "NCDFE message prefix exact");
            check(e.getMessage().endsWith(BadInitRt.class.getName()), "NCDFE message names the binary class name");
            check(e.getStackTrace().length > 0, "NCDFE carries a stack trace");
            check(e.getSuppressed().length == 0, "NCDFE has no suppressed");
        }
    }

    static void testCauseStackPreserved() {
        RuntimeException marker = MarkerHolder.MARKER;
        check(marker.getStackTrace().length > 0, "marker exception carries a stack trace from its creation site");
        check("MARKER".equals(marker.getMessage()), "marker message exact");
        check(marker instanceof RuntimeException, "marker is a RuntimeException");
    }

    static void testToStringAndMore() {
        check(new ExceptionInInitializerError().toString().equals("java.lang.ExceptionInInitializerError"),
              "toString bare form");
        check(new ExceptionInInitializerError("m").toString()
              .equals("java.lang.ExceptionInInitializerError: m"), "toString message form");
        RuntimeException c = new RuntimeException("c");
        check(new ExceptionInInitializerError(c).toString().equals("java.lang.ExceptionInInitializerError"),
              "toString with (Throwable) ctor stays bare (null message)");
        check(new ExceptionInInitializerError(c).getCause() == c, "(Throwable) cause retrievable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new ExceptionInInitializerError(c).printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.contains("Caused by: java.lang.RuntimeException: c"), "printStackTrace shows wrapped cause");
        check(out.startsWith("java.lang.ExceptionInInitializerError"), "printStackTrace bare first line");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        new ExceptionInInitializerError(c).printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).contains("Caused by:"),
              "PrintWriter overload shows cause");
        check(new ExceptionInInitializerError("x").getLocalizedMessage().equals("x"), "localized message");
        check(new ExceptionInInitializerError().getLocalizedMessage() == null, "localized null default");
    }

    static void testMarkerIdentityAcrossPaths() {
        // The same marker instance is reported no matter which trigger fires first.
        RuntimeException marker = MarkerHolder.MARKER;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        marker.printStackTrace(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8)
              .startsWith("java.lang.RuntimeException: MARKER"), "marker toString exact");
        check(marker.getCause() == null, "marker has no cause of its own");
        check(marker.getSuppressed().length == 0, "marker has no suppressed of its own");
        check(marker.fillInStackTrace() == marker, "marker fillInStackTrace returns this");
        marker.setStackTrace(new StackTraceElement[] { new StackTraceElement("K", "k", "K.java", 7) });
        check(marker.getStackTrace().length == 1, "marker accepts setStackTrace");
        check(marker.getStackTrace()[0].getLineNumber() == 7, "marker trace round-trip");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        testThrownRealClosedLoop();
        testSecondAccessNcdfe();
        testStaticExprInit();
        testStaticBlockErrorPropagates();
        testConstructorsFull();
        testInitCauseBlocked();
        testCommonContract();
        testSerializationContract();
        testReflectionAndFamily();
        testNcdfeDetails();
        testCauseStackPreserved();
        testToStringAndMore();
        testMarkerIdentityAcrossPaths();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
