/*
 * @test
 * @summary java.lang.NullPointerException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and a null dereference throwing NPE with a
 *          null message.
 * @run main NullPointerExceptionTest
 */

public class NullPointerExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NullPointerException e = new NullPointerException();
        check(e instanceof RuntimeException, "NPE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "NPE is Exception/Throwable");
    }

    static void testConstructors() {
        NullPointerException e1 = new NullPointerException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NullPointerException e2 = new NullPointerException("npe");
        check("npe".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        String s = null;
        try {
            s.length();
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            check(e instanceof RuntimeException, "JVM-thrown NPE is a RuntimeException");
        }
    }


    // ---- Deepening (lcO1, 2026-11): NullPointerException contract matrix ----
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorMatrixDeep() {
        NullPointerException none = new NullPointerException();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.NullPointerException"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        NullPointerException m = new NullPointerException("m");
        check("m".equals(m.getMessage()), "(String): message");
        check(m.getCause() == null, "(String): cause null");
        check(m.toString().equals("java.lang.NullPointerException: m"), "(String): toString exact");
        check(m.getLocalizedMessage().equals("m"), "(String): localized");
        check(new NullPointerException((String) null).getMessage() == null, "(String null): message null");
        check(new NullPointerException((String) null).toString().equals("java.lang.NullPointerException"), "(String null): bare toString");
        check("".equals(new NullPointerException("").getMessage()), "empty message kept");
        check(new NullPointerException("m").getSuppressed().length == 0, "fresh suppressed empty");
        check(new NullPointerException("m").getStackTrace().length > 0, "stack trace captured at construction");
        check(new NullPointerException() instanceof RuntimeException, "is a RuntimeException");
        check(new NullPointerException() instanceof Exception && new NullPointerException() instanceof Throwable, "is Exception/Throwable");
        check(new NullPointerException("m") instanceof java.io.Serializable, "Serializable");
        check(NullPointerException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
    }

    static void testCauseContractDeep() {
        NullPointerException t = new NullPointerException("m");
        check(t.getCause() == null, "cause starts null");
        NullPointerException c = new NullPointerException("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new NullPointerException("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        NullPointerException self = new NullPointerException();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        NullPointerException nullOk = new NullPointerException();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        NullPointerException chained = new NullPointerException("outer");
        chained.initCause(new NullPointerException("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
        check(chained.toString().equals("java.lang.NullPointerException: outer"), "toString shows only own message");
    }

    static void testSuppressedContractDeep() {
        NullPointerException t = new NullPointerException("m");
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
        NullPointerException s1 = new NullPointerException("s1");
        NullPointerException s2 = new NullPointerException("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContractDeep() {
        NullPointerException t = new NullPointerException("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("NullPointerExceptionTest"), "top frame class exact");
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
        NullPointerException t = new NullPointerException("boom");
        t.initCause(new NullPointerException("root"));
        t.addSuppressed(new NullPointerException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.NullPointerException: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.NullPointerException: sup"), "Suppressed section present");
        check(out.contains("Caused by: java.lang.NullPointerException: root"), "Caused by section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.NullPointerException: boom"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializationDeep() {
        check(java.io.ObjectStreamClass.lookup(NullPointerException.class).getSerialVersionUID() == 5162710183389028792L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            NullPointerException t = new NullPointerException("ser");
            t.initCause(new NullPointerException("root"));
            t.addSuppressed(new NullPointerException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            NullPointerException copy = (NullPointerException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.NullPointerException: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String nullStringLength() {
        try { String s = null; return "OK" + s.length(); } catch (NullPointerException e) { return "EX:" + e.getMessage(); }
    }
    static String nullArrayLoad() {
        try { int[] a = null; return "OK" + a[0]; } catch (NullPointerException e) { return "EX:" + e.getMessage(); }
    }
    static String nullArrayStore() {
        try { int[] a = null; a[0] = 1; return "OK"; } catch (NullPointerException e) { return "EX:" + e.getMessage(); }
    }
    static String nullFieldRead() {
        NpeHolder.field = null;
        try {
            return "OK" + NpeHolder.field.length();
        } catch (NullPointerException e) {
            return "EX:" + e.getMessage();
        } finally {
            NpeHolder.field = "init";
        }
    }
    static String unboxNullInteger() {
        try { Integer i = null; int v = i; return "OK" + v; } catch (NullPointerException e) { return "EX:" + e.getMessage(); }
    }
    static String syncOnNull() {
        try { Object lock = null; synchronized (lock) { } return "OK"; } catch (NullPointerException e) { return "EX:" + e.getMessage(); }
    }

    static class NpeHolder {
        static volatile String field = "init";
    }

    static void testJvmTriggerMatrix() {
        String m1 = nullStringLength();
        if (MODERN) check(m1.startsWith("EX:Cannot invoke \"String.length()\"") && m1.contains("is null"),
              "27: helpful NPE names the method (type-only lock, variable name not locked)");
        else check(m1.equals("EX:null"), "8: plain NPE message is null");
        check(nullStringLength().startsWith("EX:"), "null String dereference always throws NPE");
        check(nullArrayLoad().startsWith("EX:"), "null array load throws NPE");
        check(nullArrayStore().startsWith("EX:"), "null array store throws NPE");
        if (MODERN) check(nullFieldRead().contains("NpeHolder.field"), "27: field NPE names the field");
        else check(nullFieldRead().equals("EX:null"), "8: field NPE message is null");
        check(unboxNullInteger().startsWith("EX:"), "unboxing null Integer throws NPE");
        if (MODERN) check(unboxNullInteger().contains("Integer.intValue()"), "27: unboxing NPE names intValue()");
        check(syncOnNull().startsWith("EX:"), "synchronized (null) throws NPE");
        check("abc".length() == 3, "non-null dereference fine");
    }

    static void testRequireNonNullSources() {
        try {
            java.util.Objects.requireNonNull(null);
            throw new AssertionError("unreachable");
        } catch (NullPointerException e) {
            check(e.getMessage() == null, "requireNonNull(null) NPE has null message");
        }
        try {
            java.util.Objects.requireNonNull(null, "m1");
            throw new AssertionError("unreachable");
        } catch (NullPointerException e) {
            check("m1".equals(e.getMessage()), "requireNonNull(null,msg) NPE message exact");
        }
        String ok = java.util.Objects.requireNonNull("x", "m1");
        check(ok.equals("x"), "requireNonNull passes non-null through");
    }

    static void testCtorSurface() {
        try {
            NullPointerException.class.getConstructor(String.class, Throwable.class);
            throw new AssertionError("unreachable: (String,Throwable) ctor must be absent");
        } catch (NoSuchMethodException expected) {
            check(true, "NPE has no public (String,Throwable) constructor (on both releases)");
        }
        check(NullPointerException.class.getDeclaredConstructors().length == 2,
              "exactly two declared constructors: () and (String)");
        NullPointerException e = new NullPointerException("m");
        check(e instanceof RuntimeException, "NPE is a RuntimeException");
        check(NullPointerException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
    }

    static void testThrownInstanceDetails() {
        try {
            String s = null;
            s.length();
            throw new AssertionError("unreachable");
        } catch (NullPointerException e) {
            check(e.getCause() == null, "JVM-thrown NPE has null cause");
            check(e.getStackTrace().length > 0, "JVM-thrown NPE has stack trace");
            check(e.getSuppressed().length == 0, "JVM-thrown NPE has no suppressed");
            check(e instanceof RuntimeException, "JVM-thrown NPE is RuntimeException");
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
        testJvmTriggerMatrix();
        testRequireNonNullSources();
        testCtorSurface();
        testThrownInstanceDetails();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
