/*
 * @test
 * @summary java.lang.ArithmeticException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and the "/ by zero" message from integer
 *          division by zero.
 * @run main ArithmeticExceptionTest
 */

public class ArithmeticExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ArithmeticException e = new ArithmeticException();
        check(e instanceof RuntimeException, "ArithmeticException is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable,
              "ArithmeticException is an Exception and Throwable");
    }

    static void testConstructors() {
        ArithmeticException e1 = new ArithmeticException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ArithmeticException e2 = new ArithmeticException("divide");
        check("divide".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        int a = 1, b = 0;
        try {
            int x = a / b;
            check(x != 0, "unreachable"); // never reached: division by zero throws first
        } catch (ArithmeticException e) {
            check("/ by zero".equals(e.getMessage()), "division by zero message");
        }
    }


    // ---- Deepening (lcO1, 2026-11): ArithmeticException contract matrix ----
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorMatrixDeep() {
        ArithmeticException none = new ArithmeticException();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.ArithmeticException"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        ArithmeticException m = new ArithmeticException("m");
        check("m".equals(m.getMessage()), "(String): message");
        check(m.getCause() == null, "(String): cause null");
        check(m.toString().equals("java.lang.ArithmeticException: m"), "(String): toString exact");
        check(m.getLocalizedMessage().equals("m"), "(String): localized");
        check(new ArithmeticException((String) null).getMessage() == null, "(String null): message null");
        check(new ArithmeticException((String) null).toString().equals("java.lang.ArithmeticException"), "(String null): bare toString");
        check("".equals(new ArithmeticException("").getMessage()), "empty message kept");
        check(new ArithmeticException("m").getSuppressed().length == 0, "fresh suppressed empty");
        check(new ArithmeticException("m").getStackTrace().length > 0, "stack trace captured at construction");
        check(new ArithmeticException() instanceof RuntimeException, "is a RuntimeException");
        check(new ArithmeticException() instanceof Exception && new ArithmeticException() instanceof Throwable, "is Exception/Throwable");
        check(new ArithmeticException("m") instanceof java.io.Serializable, "Serializable");
        check(ArithmeticException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
    }

    static void testCauseContractDeep() {
        ArithmeticException t = new ArithmeticException("m");
        check(t.getCause() == null, "cause starts null");
        ArithmeticException c = new ArithmeticException("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new ArithmeticException("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        ArithmeticException self = new ArithmeticException();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        ArithmeticException nullOk = new ArithmeticException();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        ArithmeticException chained = new ArithmeticException("outer");
        chained.initCause(new ArithmeticException("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
        check(chained.toString().equals("java.lang.ArithmeticException: outer"), "toString shows only own message");
    }

    static void testSuppressedContractDeep() {
        ArithmeticException t = new ArithmeticException("m");
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
        ArithmeticException s1 = new ArithmeticException("s1");
        ArithmeticException s2 = new ArithmeticException("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContractDeep() {
        ArithmeticException t = new ArithmeticException("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("ArithmeticExceptionTest"), "top frame class exact");
        check(st[0].getMethodName().equals("testStackTraceContractDeep"), "top frame method exact");
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

    static void testPrintStackTraceContractDeep() {
        ArithmeticException t = new ArithmeticException("boom");
        t.initCause(new ArithmeticException("root"));
        t.addSuppressed(new ArithmeticException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.ArithmeticException: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.ArithmeticException: sup"), "Suppressed section present");
        check(out.contains("Caused by: java.lang.ArithmeticException: root"), "Caused by section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.ArithmeticException: boom"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializationDeep() {
        check(java.io.ObjectStreamClass.lookup(ArithmeticException.class).getSerialVersionUID() == 2256477558314496007L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            ArithmeticException t = new ArithmeticException("ser");
            t.initCause(new ArithmeticException("root"));
            t.addSuppressed(new ArithmeticException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            ArithmeticException copy = (ArithmeticException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.ArithmeticException: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void testDivisionByZeroMatrix() {
        try { int r = 1 / 0; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "int / 0 message exact"); }
        try { long r = 1L / 0L; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "long / 0 message exact"); }
        try { int r = 1 % 0; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "int % 0 message exact"); }
        try { long r = 1L % 0L; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "long % 0 message exact"); }
        try { int a = 7, b = 0; int r = a / b; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "variable int / 0 message exact"); }
        try { byte b = 1, z = 0; int r = b / z; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "byte / 0 promotes to int, same message"); }
        try { short s = 1, z = 0; int r = s / z; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "short / 0 promotes to int, same message"); }
        try { int x = 1; x /= 0; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "compound /= 0 message exact"); }
        try { int x = 1; x %= 0; } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "compound %= 0 message exact"); }
        try { int r = Math.floorDiv(1, 0); } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "floorDiv / 0 message exact"); }
        try { int r = Math.floorMod(1, 0); } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "floorMod / 0 message exact"); }
        try { long r = Math.floorDiv(1L, 0L); } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "long floorDiv / 0 message exact"); }
        try { long r = Math.floorMod(1L, 0L); } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "long floorMod / 0 message exact"); }
        try { int r = Math.floorDiv(-7, 0); } catch (ArithmeticException e) { check("/ by zero".equals(e.getMessage()), "floorDiv(-7,0) message exact"); }
    }

    static void testOverflowExactMatrix() {
        try { int r = Math.addExact(Integer.MAX_VALUE, 1); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "addExact int message exact"); }
        try { int r = Math.subtractExact(Integer.MIN_VALUE, 1); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "subtractExact int message exact"); }
        try { int r = Math.multiplyExact(Integer.MAX_VALUE, 2); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "multiplyExact int message exact"); }
        try { int r = Math.negateExact(Integer.MIN_VALUE); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "negateExact int message exact"); }
        try { int r = Math.incrementExact(Integer.MAX_VALUE); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "incrementExact int message exact"); }
        try { int r = Math.decrementExact(Integer.MIN_VALUE); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "decrementExact int message exact"); }
        try { int r = Math.toIntExact(Long.MAX_VALUE); } catch (ArithmeticException e) { check("integer overflow".equals(e.getMessage()), "toIntExact message exact"); }
        try { long r = Math.addExact(Long.MAX_VALUE, 1L); } catch (ArithmeticException e) { check("long overflow".equals(e.getMessage()), "addExact long message exact"); }
        try { long r = Math.subtractExact(Long.MIN_VALUE, 1L); } catch (ArithmeticException e) { check("long overflow".equals(e.getMessage()), "subtractExact long message exact"); }
        try { long r = Math.multiplyExact(Long.MIN_VALUE, 2L); } catch (ArithmeticException e) { check("long overflow".equals(e.getMessage()), "multiplyExact long message exact"); }
        try { long r = Math.negateExact(Long.MIN_VALUE); } catch (ArithmeticException e) { check("long overflow".equals(e.getMessage()), "negateExact long message exact"); }
    }

    static void testNoOverflowCases() {
        check(Math.addExact(Integer.MAX_VALUE - 1, 1) == Integer.MAX_VALUE, "addExact in range succeeds");
        check(Integer.MIN_VALUE / -1 == Integer.MIN_VALUE, "MIN_VALUE / -1 wraps silently (no exception)");
        check(Integer.MIN_VALUE % -1 == 0, "MIN_VALUE % -1 is 0 (no exception)");
        check(7 / 2 == 3 && -7 / 2 == -3, "normal division unaffected");
        check(0 / 5 == 0, "zero dividend fine");
        check(Math.floorDiv(-7, 2) == -4 && Math.floorMod(-7, 2) == 1, "floorDiv/floorMod semantics intact");
    }

    static void testThrownInstanceDetails() {
        try {
            int r = 1 / 0;
            throw new AssertionError("unreachable");
        } catch (ArithmeticException e) {
            check(e.getCause() == null, "JVM-thrown AE has null cause");
            check(e.getStackTrace().length > 0, "JVM-thrown AE has stack trace");
            check(e.getSuppressed().length == 0, "JVM-thrown AE has no suppressed");
            check(e.toString().equals("java.lang.ArithmeticException: / by zero"), "toString exact");
            check(e instanceof RuntimeException, "JVM-thrown AE is RuntimeException");
        }
    }

    static void testAbsExactAbsent() {
        try {
            Math.class.getMethod("absExact", int.class);
            throw new AssertionError("unreachable: absExact must be absent on JDK-8");
        } catch (NoSuchMethodException expected) {
            check(true, "Math.absExact absent on JDK-8 (added in JDK 15)");
        }
    }
    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        testCtorMatrixDeep();
        testCauseContractDeep();
        testSuppressedContractDeep();
        testStackTraceContractDeep();
        testPrintStackTraceContractDeep();
        testSuidAndSerializationDeep();
        testDivisionByZeroMatrix();
        testOverflowExactMatrix();
        testNoOverflowCases();
        testThrownInstanceDetails();
        testAbsExactAbsent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
