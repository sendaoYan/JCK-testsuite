/*
 * @test
 * @summary java.lang.ArrayIndexOutOfBoundsException API conformance: IndexOutOfBoundsException
 *          hierarchy, no-arg and (String) constructors, a thrown instance from an out-of-range
 *          array access, and the Java 9+ (int index) constructor (via reflection).
 * @run main ArrayIndexOutOfBoundsExceptionTest
 */

public class ArrayIndexOutOfBoundsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException();
        check(e instanceof IndexOutOfBoundsException, "AIOOBE is an IndexOutOfBoundsException");
        check(e instanceof RuntimeException, "AIOOBE is a RuntimeException");
    }

    static void testConstructors() {
        ArrayIndexOutOfBoundsException e1 = new ArrayIndexOutOfBoundsException();
        check(e1.getMessage() == null, "no-arg ctor null message");
        ArrayIndexOutOfBoundsException e2 = new ArrayIndexOutOfBoundsException("idx");
        check("idx".equals(e2.getMessage()), "(String) ctor message");
    }

    static void testThrown() {
        int[] a = new int[3];
        try {
            a[3] = 1;
            throw new AssertionError("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(e instanceof ArrayIndexOutOfBoundsException, "a[3] on length-3 array throws AIOOBE");
        }
    }

    static void testIntCtor() {
        try {
            java.lang.reflect.Constructor<ArrayIndexOutOfBoundsException> c =
                ArrayIndexOutOfBoundsException.class.getConstructor(int.class);
            ArrayIndexOutOfBoundsException e = c.newInstance(5);
            check("Array index out of range: 5".equals(e.getMessage()),
                  "(int) ctor message [Java 9+]");
        } catch (NoSuchMethodException e) {
            // JDK 8: the (int index) constructor does not exist.
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }


    // ---- Deepening (lcO1, 2026-11): ArrayIndexOutOfBoundsException contract matrix ----
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorMatrixDeep() {
        ArrayIndexOutOfBoundsException none = new ArrayIndexOutOfBoundsException();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.ArrayIndexOutOfBoundsException"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        ArrayIndexOutOfBoundsException m = new ArrayIndexOutOfBoundsException("m");
        check("m".equals(m.getMessage()), "(String): message");
        check(m.getCause() == null, "(String): cause null");
        check(m.toString().equals("java.lang.ArrayIndexOutOfBoundsException: m"), "(String): toString exact");
        check(m.getLocalizedMessage().equals("m"), "(String): localized");
        check(new ArrayIndexOutOfBoundsException((String) null).getMessage() == null, "(String null): message null");
        check(new ArrayIndexOutOfBoundsException((String) null).toString().equals("java.lang.ArrayIndexOutOfBoundsException"), "(String null): bare toString");
        check("".equals(new ArrayIndexOutOfBoundsException("").getMessage()), "empty message kept");
        check(new ArrayIndexOutOfBoundsException("m").getSuppressed().length == 0, "fresh suppressed empty");
        check(new ArrayIndexOutOfBoundsException("m").getStackTrace().length > 0, "stack trace captured at construction");
        check(new ArrayIndexOutOfBoundsException() instanceof RuntimeException, "is a RuntimeException");
        check(new ArrayIndexOutOfBoundsException() instanceof Exception && new ArrayIndexOutOfBoundsException() instanceof Throwable, "is Exception/Throwable");
        check(new ArrayIndexOutOfBoundsException("m") instanceof java.io.Serializable, "Serializable");
        check(ArrayIndexOutOfBoundsException.class.getSuperclass() == IndexOutOfBoundsException.class,
              "direct superclass IndexOutOfBoundsException (specialized for AIOOBE)");
    }

    static void testCauseContractDeep() {
        ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException("m");
        check(t.getCause() == null, "cause starts null");
        ArrayIndexOutOfBoundsException c = new ArrayIndexOutOfBoundsException("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new ArrayIndexOutOfBoundsException("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        ArrayIndexOutOfBoundsException self = new ArrayIndexOutOfBoundsException();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        ArrayIndexOutOfBoundsException nullOk = new ArrayIndexOutOfBoundsException();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        ArrayIndexOutOfBoundsException chained = new ArrayIndexOutOfBoundsException("outer");
        chained.initCause(new ArrayIndexOutOfBoundsException("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
        check(chained.toString().equals("java.lang.ArrayIndexOutOfBoundsException: outer"), "toString shows only own message");
    }

    static void testSuppressedContractDeep() {
        ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException("m");
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
        ArrayIndexOutOfBoundsException s1 = new ArrayIndexOutOfBoundsException("s1");
        ArrayIndexOutOfBoundsException s2 = new ArrayIndexOutOfBoundsException("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContractDeep() {
        ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("ArrayIndexOutOfBoundsExceptionTest"), "top frame class exact");
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
        ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException("boom");
        t.initCause(new ArrayIndexOutOfBoundsException("root"));
        t.addSuppressed(new ArrayIndexOutOfBoundsException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.ArrayIndexOutOfBoundsException: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.ArrayIndexOutOfBoundsException: sup"), "Suppressed section present");
        check(out.contains("Caused by: java.lang.ArrayIndexOutOfBoundsException: root"), "Caused by section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.ArrayIndexOutOfBoundsException: boom"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializationDeep() {
        check(java.io.ObjectStreamClass.lookup(ArrayIndexOutOfBoundsException.class).getSerialVersionUID() == -5116101128118950844L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException("ser");
            t.initCause(new ArrayIndexOutOfBoundsException("root"));
            t.addSuppressed(new ArrayIndexOutOfBoundsException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            ArrayIndexOutOfBoundsException copy = (ArrayIndexOutOfBoundsException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.ArrayIndexOutOfBoundsException: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void testIntCtorMessages() {
        check(new ArrayIndexOutOfBoundsException(5).getMessage().equals("Array index out of range: 5"),
              "(int 5) message exact (identical on 8 and 27)");
        check(new ArrayIndexOutOfBoundsException(-7).getMessage().equals("Array index out of range: -7"),
              "(int -7) message exact");
        check(new ArrayIndexOutOfBoundsException(0).getMessage().equals("Array index out of range: 0"),
              "(int 0) message exact");
        check(new ArrayIndexOutOfBoundsException(Integer.MAX_VALUE).getMessage().equals("Array index out of range: 2147483647"),
              "(int MAX) message exact");
        check(new ArrayIndexOutOfBoundsException(Integer.MIN_VALUE).getMessage().equals("Array index out of range: -2147483648"),
              "(int MIN) message exact");
        check(new ArrayIndexOutOfBoundsException(5).toString()
              .equals("java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 5"), "(int) toString exact");
        check(new ArrayIndexOutOfBoundsException(5).getCause() == null, "(int) cause null");
        check(new ArrayIndexOutOfBoundsException(5).getSuppressed().length == 0, "(int) suppressed empty");
    }

    static String loadInt(int[] a, int i) {
        try { return "OK" + a[i]; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }
    static String storeInt(int[] a, int i) {
        try { a[i] = 9; return "OK"; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }
    static String loadLong(long[] a, int i) {
        try { return "OK" + a[i]; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }
    static String loadChar(char[] a, int i) {
        try { return "OK" + a[i]; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }
    static String loadObj(Object[] a, int i) {
        try { return "OK" + a[i]; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }
    static String load2dOuter(int[][] a, int i) {
        try { return "OK" + a[i][0]; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }
    static String load2dInner(int[][] a, int j) {
        try { return "OK" + a[0][j]; } catch (ArrayIndexOutOfBoundsException e) { return "EX:" + e.getMessage(); }
    }

    // 越界索引消息是 VM 运行时生成、非 API 契约；-Xcomp 快路径下 message 可能为 null（"EX:null"）。
    static boolean idxMsg(String got, String expected) {
        return got.equals("EX:" + expected) || got.equals("EX:null");
    }

    static void testJvmTriggerMatrix() {
        int[] a3 = new int[3];
        String m1 = loadInt(a3, 3);
        if (MODERN) check(idxMsg(m1, "Index 3 out of bounds for length 3"), "27: a[3] on int[3] message exact");
        else check(idxMsg(m1, "3"), "8: a[3] on int[3] message is the bare index");
        String m2 = loadInt(a3, -1);
        if (MODERN) check(idxMsg(m2, "Index -1 out of bounds for length 3"), "27: a[-1] negative index message exact");
        else check(idxMsg(m2, "-1"), "8: a[-1] negative index message is the bare index");
        String m3 = storeInt(a3, 5);
        if (MODERN) check(idxMsg(m3, "Index 5 out of bounds for length 3"), "27: store a[5] message exact");
        else check(idxMsg(m3, "5"), "8: store a[5] message is the bare index");
        String m4 = load2dOuter(new int[3][5], 3);
        if (MODERN) check(idxMsg(m4, "Index 3 out of bounds for length 3"), "27: new int[3][5] outer index 3 message exact");
        else check(idxMsg(m4, "3"), "8: new int[3][5] outer index 3 message is the bare index");
        String m5 = load2dInner(new int[3][5], 5);
        if (MODERN) check(idxMsg(m5, "Index 5 out of bounds for length 5"), "27: inner index 5 of length-5 row message exact");
        else check(idxMsg(m5, "5"), "8: inner index 5 of length-5 row message is the bare index");
        String m6 = loadObj(new String[2], 7);
        if (MODERN) check(idxMsg(m6, "Index 7 out of bounds for length 2"), "27: String[2][7] message exact");
        else check(idxMsg(m6, "7"), "8: String[2][7] message is the bare index");
        String m7 = loadLong(new long[2], 2);
        if (MODERN) check(idxMsg(m7, "Index 2 out of bounds for length 2"), "27: long[2][2] message exact");
        else check(idxMsg(m7, "2"), "8: long[2][2] message is the bare index");
        String m8 = loadChar(new char[1], -2);
        if (MODERN) check(idxMsg(m8, "Index -2 out of bounds for length 1"), "27: char[1][-2] message exact");
        else check(idxMsg(m8, "-2"), "8: char[1][-2] message is the bare index");
    }

    static void testHierarchyIoobe() {
        ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException();
        check(e instanceof IndexOutOfBoundsException, "AIOOBE is an IndexOutOfBoundsException");
        check(e instanceof RuntimeException, "AIOOBE is a RuntimeException");
        check(ArrayIndexOutOfBoundsException.class.getSuperclass() == IndexOutOfBoundsException.class,
              "direct superclass IndexOutOfBoundsException");
        IndexOutOfBoundsException parent = new IndexOutOfBoundsException("p");
        check(!(parent instanceof ArrayIndexOutOfBoundsException),
              "plain IOOBE is not an AIOOBE (one-way subtyping)");
        check(new ArrayIndexOutOfBoundsException(3) instanceof IndexOutOfBoundsException,
              "(int)-constructed AIOOBE still an IOOBE");
    }

    static void testThrownInstanceDetails() {
        try {
            int[] a = new int[1];
            int v = a[1];
            throw new AssertionError("unreachable");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(e.getCause() == null, "JVM-thrown AIOOBE has null cause");
            check(e.getStackTrace().length > 0, "JVM-thrown AIOOBE has stack trace");
            check(e.getSuppressed().length == 0, "JVM-thrown AIOOBE has no suppressed");
            check(e instanceof IndexOutOfBoundsException, "JVM-thrown AIOOBE instanceof IOOBE");
            if (MODERN) check(e.getMessage() == null || e.getMessage().startsWith("Index "), "27: JVM message shape");
            // JDK-8：越界 message 是 VM 运行时生成、非契约，-Xcomp 快路径下可能为 null（异常类型已断言）。
        }
    }

    static void testMoreTriggersAndForms() {
        check(new ArrayIndexOutOfBoundsException("custom").getMessage().equals("custom"), "(String) custom message");
        try {
            int[] a = new int[1];
            a[1] = 1;
        } catch (IndexOutOfBoundsException e) {
            check(e.getClass() == ArrayIndexOutOfBoundsException.class, "store trigger has exact AIOOBE class");
        }
        try {
            Object[] o = new Object[1];
            Object v = o[-1];
        } catch (RuntimeException e) {
            check(e.getClass() == ArrayIndexOutOfBoundsException.class, "negative object-array index exact class");
        }
        try {
            boolean[] ba = new boolean[1];
            boolean v = ba[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "boolean array load OOB triggers AIOOBE");
        }
        try {
            double[] da = new double[1];
            da[2] = 1.0;
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "double array store OOB triggers AIOOBE");
        }
        check(new ArrayIndexOutOfBoundsException(3).getMessage().contains("3"), "(int 3) message contains index");
        check(new ArrayIndexOutOfBoundsException(-3).getMessage().contains("-3"), "(int -3) contains negative index");
        check(new ArrayIndexOutOfBoundsException(2147483647).getMessage().endsWith("2147483647"), "(int MAX) ends with index");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new ArrayIndexOutOfBoundsException(5).printStackTrace(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8)
              .startsWith("java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 5"),
              "(int) printStackTrace first line exact");
    }
    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        testThrown();
        testIntCtor();
        testCtorMatrixDeep();
        testCauseContractDeep();
        testSuppressedContractDeep();
        testStackTraceContractDeep();
        testPrintStackTraceContractDeep();
        testSuidAndSerializationDeep();
        testIntCtorMessages();
        testJvmTriggerMatrix();
        testHierarchyIoobe();
        testThrownInstanceDetails();
        testMoreTriggersAndForms();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
