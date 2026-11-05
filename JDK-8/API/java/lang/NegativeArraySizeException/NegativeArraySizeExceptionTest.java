/*
 * @test
 * @summary java.lang.NegativeArraySizeException API conformance: RuntimeException hierarchy,
 *          no-arg and (String) constructors, and a thrown instance from new int[-1] with the
 *          size as its message.
 * @run main NegativeArraySizeExceptionTest
 */

public class NegativeArraySizeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NegativeArraySizeException e = new NegativeArraySizeException();
        check(e instanceof RuntimeException, "NASE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "NASE is Exception/Throwable");
    }

    static void testConstructors() {
        NegativeArraySizeException e1 = new NegativeArraySizeException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NegativeArraySizeException e2 = new NegativeArraySizeException("neg");
        check("neg".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        try {
            int[] a = new int[-1];
            throw new AssertionError("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException e) {
            check(e instanceof NegativeArraySizeException,
                  "new int[-1] throws NegativeArraySizeException");
        }
    }


    // ---- Deepening (lcO1, 2026-11): NegativeArraySizeException contract matrix ----
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorMatrixDeep() {
        NegativeArraySizeException none = new NegativeArraySizeException();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.NegativeArraySizeException"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        NegativeArraySizeException m = new NegativeArraySizeException("m");
        check("m".equals(m.getMessage()), "(String): message");
        check(m.getCause() == null, "(String): cause null");
        check(m.toString().equals("java.lang.NegativeArraySizeException: m"), "(String): toString exact");
        check(m.getLocalizedMessage().equals("m"), "(String): localized");
        check(new NegativeArraySizeException((String) null).getMessage() == null, "(String null): message null");
        check(new NegativeArraySizeException((String) null).toString().equals("java.lang.NegativeArraySizeException"), "(String null): bare toString");
        check("".equals(new NegativeArraySizeException("").getMessage()), "empty message kept");
        check(new NegativeArraySizeException("m").getSuppressed().length == 0, "fresh suppressed empty");
        check(new NegativeArraySizeException("m").getStackTrace().length > 0, "stack trace captured at construction");
        check(new NegativeArraySizeException() instanceof RuntimeException, "is a RuntimeException");
        check(new NegativeArraySizeException() instanceof Exception && new NegativeArraySizeException() instanceof Throwable, "is Exception/Throwable");
        check(new NegativeArraySizeException("m") instanceof java.io.Serializable, "Serializable");
        check(NegativeArraySizeException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
    }

    static void testCauseContractDeep() {
        NegativeArraySizeException t = new NegativeArraySizeException("m");
        check(t.getCause() == null, "cause starts null");
        NegativeArraySizeException c = new NegativeArraySizeException("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new NegativeArraySizeException("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        NegativeArraySizeException self = new NegativeArraySizeException();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        NegativeArraySizeException nullOk = new NegativeArraySizeException();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        NegativeArraySizeException chained = new NegativeArraySizeException("outer");
        chained.initCause(new NegativeArraySizeException("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
        check(chained.toString().equals("java.lang.NegativeArraySizeException: outer"), "toString shows only own message");
    }

    static void testSuppressedContractDeep() {
        NegativeArraySizeException t = new NegativeArraySizeException("m");
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
        NegativeArraySizeException s1 = new NegativeArraySizeException("s1");
        NegativeArraySizeException s2 = new NegativeArraySizeException("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContractDeep() {
        NegativeArraySizeException t = new NegativeArraySizeException("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("NegativeArraySizeExceptionTest"), "top frame class exact");
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
        NegativeArraySizeException t = new NegativeArraySizeException("boom");
        t.initCause(new NegativeArraySizeException("root"));
        t.addSuppressed(new NegativeArraySizeException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.NegativeArraySizeException: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.NegativeArraySizeException: sup"), "Suppressed section present");
        check(out.contains("Caused by: java.lang.NegativeArraySizeException: root"), "Caused by section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.NegativeArraySizeException: boom"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializationDeep() {
        check(java.io.ObjectStreamClass.lookup(NegativeArraySizeException.class).getSerialVersionUID() == -8960118058596991861L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            NegativeArraySizeException t = new NegativeArraySizeException("ser");
            t.initCause(new NegativeArraySizeException("root"));
            t.addSuppressed(new NegativeArraySizeException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            NegativeArraySizeException copy = (NegativeArraySizeException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.NegativeArraySizeException: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String newIntArr(int n) {
        try { return "OK" + ((int[]) java.lang.reflect.Array.newInstance(int.class, n)).length; }
        catch (NegativeArraySizeException e) { return "EX:" + e.getMessage(); }
    }

    static void testJvmTriggerMatrix() {
        String m1;
        try { int[] a = new int[-1]; m1 = "OK"; } catch (NegativeArraySizeException e) { m1 = "EX:" + e.getMessage(); }
        if (MODERN) check(m1.equals("EX:-1"), "27: new int[-1] message is the size as text");
        else check(m1.equals("EX:null"), "8: new int[-1] message is null");
        String m2;
        try { int[] a = new int[-7]; m2 = "OK"; } catch (NegativeArraySizeException e) { m2 = "EX:" + e.getMessage(); }
        if (MODERN) check(m2.equals("EX:-7"), "27: new int[-7] message exact");
        else check(m2.equals("EX:null"), "8: new int[-7] message is null");
        String m3;
        try { int[] a = new int[Integer.MIN_VALUE]; m3 = "OK"; } catch (NegativeArraySizeException e) { m3 = "EX:" + e.getMessage(); }
        if (MODERN) check(m3.equals("EX:-2147483648"), "27: new int[MIN_VALUE] message exact");
        else check(m3.equals("EX:null"), "8: new int[MIN_VALUE] message is null");
        try {
            Object[] o = new Object[-1];
            throw new AssertionError("unreachable");
        } catch (NegativeArraySizeException e) {
            check(true, "new Object[-1] throws NASE");
        }
        try {
            byte[] b = new byte[-2];
            throw new AssertionError("unreachable");
        } catch (NegativeArraySizeException e) {
            check(true, "new byte[-2] throws NASE");
        }
        try {
            String[][] s = new String[1][-3];
            throw new AssertionError("unreachable");
        } catch (NegativeArraySizeException e) {
            check(true, "second dimension new String[1][-3] throws NASE");
        }
        String m4 = newIntArr(-1);
        if (MODERN) check(m4.equals("EX:-1"), "27: Array.newInstance(int.class,-1) message exact");
        else check(m4.equals("EX:null"), "8: Array.newInstance(int.class,-1) message is null");
        check(newIntArr(3).equals("OK3"), "positive size via reflection fine");
        check(new int[0].length == 0, "zero-size array fine");
    }

    static void testHierarchyDeep() {
        NegativeArraySizeException e = new NegativeArraySizeException();
        check(e instanceof RuntimeException, "NASE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "NASE is Exception/Throwable");
        check(NegativeArraySizeException.class.getSuperclass() == RuntimeException.class,
              "direct superclass RuntimeException");
        try {
            int[] a = new int[0];
            check(a.length == 0, "zero-length allocation never throws");
        } catch (NegativeArraySizeException unexpected) {
            throw new AssertionError("unexpected", unexpected);
        }
    }

    static void testReflectionNegativeMore() {
        String r;
        try {
            Object o = java.lang.reflect.Array.newInstance(String.class, -5);
            r = "OK";
        } catch (NegativeArraySizeException e) {
            r = "EX:" + e.getMessage();
        }
        if (MODERN) check(r.equals("EX:-5"), "27: Array.newInstance(String,-5) message exact");
        else check(r.equals("EX:null"), "8: Array.newInstance(String,-5) message null");
        check(newIntArr(0).equals("OK0"), "zero length via reflection fine");
        try {
            Object o = java.lang.reflect.Array.newInstance(Object.class, -1);
            throw new AssertionError("unreachable");
        } catch (NegativeArraySizeException e) {
            check(true, "Array.newInstance(Object,-1) throws NASE");
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
        testHierarchyDeep();
        testReflectionNegativeMore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
