/*
 * @test
 * @summary java.lang.IndexOutOfBoundsException conformance: white-box structure + black-box behavior.
 * @run main IndexOutOfBoundsExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IndexOutOfBoundsExceptionTest {
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
        Class<?> c = Class.forName("java.lang.IndexOutOfBoundsException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IndexOutOfBoundsException");

        IndexOutOfBoundsException e0 = new IndexOutOfBoundsException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { IndexOutOfBoundsException e = new IndexOutOfBoundsException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { IndexOutOfBoundsException e = new IndexOutOfBoundsException(5); check(e.getMessage() != null, "(int) ctor non-null message"); }
        { IndexOutOfBoundsException e = new IndexOutOfBoundsException(5L); check(e.getMessage() != null, "(long) ctor non-null message"); }

        // common Throwable contract
        IndexOutOfBoundsException t = new IndexOutOfBoundsException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.IndexOutOfBoundsException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }


    // ---- Deepening (lcO1, 2026-11): IndexOutOfBoundsException contract matrix ----
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testCtorMatrixDeep() {
        IndexOutOfBoundsException none = new IndexOutOfBoundsException();
        check(none.getMessage() == null && none.getCause() == null, "no-arg: null message and cause");
        check(none.toString().equals("java.lang.IndexOutOfBoundsException"), "no-arg toString bare");
        check(none.getLocalizedMessage() == null, "no-arg localized null");
        IndexOutOfBoundsException m = new IndexOutOfBoundsException("m");
        check("m".equals(m.getMessage()), "(String): message");
        check(m.getCause() == null, "(String): cause null");
        check(m.toString().equals("java.lang.IndexOutOfBoundsException: m"), "(String): toString exact");
        check(m.getLocalizedMessage().equals("m"), "(String): localized");
        check(new IndexOutOfBoundsException((String) null).getMessage() == null, "(String null): message null");
        check(new IndexOutOfBoundsException((String) null).toString().equals("java.lang.IndexOutOfBoundsException"), "(String null): bare toString");
        check("".equals(new IndexOutOfBoundsException("").getMessage()), "empty message kept");
        check(new IndexOutOfBoundsException("m").getSuppressed().length == 0, "fresh suppressed empty");
        check(new IndexOutOfBoundsException("m").getStackTrace().length > 0, "stack trace captured at construction");
        check(new IndexOutOfBoundsException() instanceof RuntimeException, "is a RuntimeException");
        check(new IndexOutOfBoundsException() instanceof Exception && new IndexOutOfBoundsException() instanceof Throwable, "is Exception/Throwable");
        check(new IndexOutOfBoundsException("m") instanceof java.io.Serializable, "Serializable");
        check(IndexOutOfBoundsException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
    }

    static void testCauseContractDeep() {
        IndexOutOfBoundsException t = new IndexOutOfBoundsException("m");
        check(t.getCause() == null, "cause starts null");
        IndexOutOfBoundsException c = new IndexOutOfBoundsException("c");
        check(t.initCause(c) == t, "initCause returns this");
        check(t.getCause() == c, "initCause stores identity");
        check(t.getMessage().equals("m"), "initCause leaves message alone");
        try {
            t.initCause(new IndexOutOfBoundsException("other"));
            throw new AssertionError("unreachable: second initCause");
        } catch (IllegalStateException e) {
            check(e.getMessage().startsWith("Can't overwrite cause with"), "second initCause ISE message prefix");
        }
        IndexOutOfBoundsException self = new IndexOutOfBoundsException();
        try {
            self.initCause(self);
            throw new AssertionError("unreachable: self initCause");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "self initCause IAE message exact");
        }
        IndexOutOfBoundsException nullOk = new IndexOutOfBoundsException();
        check(nullOk.initCause(null) == nullOk && nullOk.getCause() == null, "initCause(null) accepted");
        IndexOutOfBoundsException chained = new IndexOutOfBoundsException("outer");
        chained.initCause(new IndexOutOfBoundsException("inner"));
        check(chained.getCause().getMessage().equals("inner"), "chained cause readable");
        check(chained.toString().equals("java.lang.IndexOutOfBoundsException: outer"), "toString shows only own message");
    }

    static void testSuppressedContractDeep() {
        IndexOutOfBoundsException t = new IndexOutOfBoundsException("m");
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
        IndexOutOfBoundsException s1 = new IndexOutOfBoundsException("s1");
        IndexOutOfBoundsException s2 = new IndexOutOfBoundsException("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        check(t.getSuppressed().length == 2, "two suppressed kept");
        check(t.getSuppressed()[0] == s1 && t.getSuppressed()[1] == s2, "suppressed insertion order");
        t.getSuppressed()[0] = null;
        check(t.getSuppressed()[0] == s1, "suppressed defensive copy");
    }

    static void testStackTraceContractDeep() {
        IndexOutOfBoundsException t = new IndexOutOfBoundsException("m");
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("IndexOutOfBoundsExceptionTest"), "top frame class exact");
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
        IndexOutOfBoundsException t = new IndexOutOfBoundsException("boom");
        t.initCause(new IndexOutOfBoundsException("root"));
        t.addSuppressed(new IndexOutOfBoundsException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.IndexOutOfBoundsException: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.IndexOutOfBoundsException: sup"), "Suppressed section present");
        check(out.contains("Caused by: java.lang.IndexOutOfBoundsException: root"), "Caused by section present");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.IndexOutOfBoundsException: boom"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializationDeep() {
        check(java.io.ObjectStreamClass.lookup(IndexOutOfBoundsException.class).getSerialVersionUID() == 234122996006267687L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        try {
            IndexOutOfBoundsException t = new IndexOutOfBoundsException("ser");
            t.initCause(new IndexOutOfBoundsException("root"));
            t.addSuppressed(new IndexOutOfBoundsException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            IndexOutOfBoundsException copy = (IndexOutOfBoundsException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getCause() != null && "root".equals(copy.getCause().getMessage()), "round-trip keeps cause");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.IndexOutOfBoundsException: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String listGet(java.util.List<String> l, int i) {
        try { return "OK" + l.get(i); } catch (IndexOutOfBoundsException e) { return "EX:" + e.getClass().getSimpleName() + ":" + e.getMessage(); }
    }
    static String charAt(String s, int i) {
        try { return "OK" + s.charAt(i); } catch (IndexOutOfBoundsException e) { return "EX:" + e.getClass().getSimpleName() + ":" + e.getMessage(); }
    }

    static void testTriggerSources() {
        String r1 = listGet(java.util.Arrays.asList("a"), 5);
        if (MODERN) check(r1.equals("EX:ArrayIndexOutOfBoundsException:Index 5 out of bounds for length 1"),
              "27: Arrays.asList get(5) is AIOOBE with exact message");
        else check(r1.equals("EX:ArrayIndexOutOfBoundsException:5"),
              "8: Arrays.asList get(5) is AIOOBE with bare-index message");
        String r2 = charAt("abc", 7);
        if (MODERN) check(r2.equals("EX:StringIndexOutOfBoundsException:Index 7 out of bounds for length 3"),
              "27: String.charAt(7) SIOOBE message exact");
        else check(r2.equals("EX:StringIndexOutOfBoundsException:String index out of range: 7"),
              "8: String.charAt(7) SIOOBE message exact");
        try {
            char c = new StringBuilder("ab").charAt(9);
            throw new AssertionError("unreachable");
        } catch (IndexOutOfBoundsException e) {
            check(e instanceof StringIndexOutOfBoundsException, "StringBuilder.charAt throws SIOOBE, an IOOBE subtype");
        }
    }

    static void testSubtypeFamily() {
        IndexOutOfBoundsException e = new IndexOutOfBoundsException("m");
        check(e instanceof RuntimeException, "IOOBE is a RuntimeException");
        check(e instanceof Exception && e instanceof Throwable, "IOOBE is Exception/Throwable");
        check(ArrayIndexOutOfBoundsException.class.getSuperclass() == IndexOutOfBoundsException.class,
              "AIOOBE directly extends IOOBE");
        check(StringIndexOutOfBoundsException.class.getSuperclass() == IndexOutOfBoundsException.class,
              "SIOOBE directly extends IOOBE");
        check(new ArrayIndexOutOfBoundsException(1) instanceof IndexOutOfBoundsException,
              "AIOOBE instances are IOOBE");
        check("abc".charAt(0) == 'a', "in-bounds charAt fine");
        check(java.util.Arrays.asList("a").get(0).equals("a"), "in-bounds list get fine");
    }

    static void testIntLongCtorMessages() {
        check(new IndexOutOfBoundsException(5).getMessage().equals("Index out of range: 5"),
              "(int 5) message exact (JDK 9+ ctor)");
        check(new IndexOutOfBoundsException(-3).getMessage().equals("Index out of range: -3"),
              "(int -3) message exact");
        check(new IndexOutOfBoundsException(0).getMessage().equals("Index out of range: 0"),
              "(int 0) message exact");
        check(new IndexOutOfBoundsException(Integer.MAX_VALUE).getMessage().equals("Index out of range: 2147483647"),
              "(int MAX) message exact");
        check(new IndexOutOfBoundsException(5L).getMessage().equals("Index out of range: 5"),
              "(long 5) message exact");
        check(new IndexOutOfBoundsException(Long.MIN_VALUE).getMessage().equals("Index out of range: -9223372036854775808"),
              "(long MIN) message exact");
        check(new IndexOutOfBoundsException(5).toString()
              .equals("java.lang.IndexOutOfBoundsException: Index out of range: 5"), "(int) toString exact");
        check(new IndexOutOfBoundsException(5).getCause() == null, "(int) cause null");
        check(new IndexOutOfBoundsException(5).getSuppressed().length == 0, "(int) suppressed empty");
    }

    static void testCollectionTriggerSources() {
        try {
            java.util.List<String> l = new java.util.ArrayList<String>();
            l.add(5, "x");
        } catch (IndexOutOfBoundsException e) {
            check("Index: 5, Size: 0".equals(e.getMessage()), "ArrayList.add(5) message exact (identical 8/27)");
            check(e.getClass() == IndexOutOfBoundsException.class, "ArrayList.add OOB exact class IOOBE");
        }
        try {
            java.util.List<String> l = new java.util.ArrayList<String>();
            l.add("a");
            l.add(5, "x");
        } catch (IndexOutOfBoundsException e) {
            check("Index: 5, Size: 1".equals(e.getMessage()), "ArrayList.add(5) on size-1 message exact");
        }
        try {
            java.util.List<String> l = new java.util.ArrayList<String>();
            l.add("a");
            java.util.List<String> s = l.subList(0, 5);
        } catch (IndexOutOfBoundsException e) {
            check("toIndex = 5".equals(e.getMessage()), "subList toIndex message exact (identical 8/27)");
        }
        try {
            java.util.List<String> l = new java.util.ArrayList<String>();
            l.add("a");
            String prev = l.set(3, "x");
        } catch (IndexOutOfBoundsException e) {
            if (MODERN) check("Index 3 out of bounds for length 1".equals(e.getMessage()), "27: ArrayList.set(3) message exact");
            else check("Index: 3, Size: 1".equals(e.getMessage()), "8: ArrayList.set(3) message exact");
        }
        try {
            int cp = "ab".codePointAt(9);
        } catch (StringIndexOutOfBoundsException e) {
            if (MODERN) check("Index 9 out of bounds for length 2".equals(e.getMessage()), "27: codePointAt message exact");
            else check("String index out of range: 9".equals(e.getMessage()), "8: codePointAt message exact");
            check(e instanceof IndexOutOfBoundsException, "SIOOBE from codePointAt is an IOOBE");
        }
        try {
            CharSequence cs = "ab".subSequence(1, 9);
        } catch (StringIndexOutOfBoundsException e) {
            if (MODERN) check("Range [1, 9) out of bounds for length 2".equals(e.getMessage()), "27: subSequence range message exact");
            else check("String index out of range: 9".equals(e.getMessage()), "8: subSequence message exact");
        }
        try {
            java.util.List<String> l = new java.util.ArrayList<String>();
            l.add("a");
            l.add(0, "b");
            check(l.get(0).equals("b"), "in-bounds add shifts elements");
        } catch (IndexOutOfBoundsException e) {
            throw new AssertionError("unexpected", e);
        }
    }

    static void testCtorExtras() {
        check(new IndexOutOfBoundsException("m").getLocalizedMessage().equals("m"), "localized message");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new IndexOutOfBoundsException("boom").printStackTrace(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8)
              .startsWith("java.lang.IndexOutOfBoundsException: boom"), "printStackTrace first line exact");
        check(new IndexOutOfBoundsException().getSuppressed().length == 0, "() suppressed empty");
    }

    static void testMoreBoundsForms() {
        try {
            CharSequence cs = "abc".subSequence(0, 3);
            check(cs.toString().equals("abc"), "in-bounds subSequence fine");
        } catch (IndexOutOfBoundsException e) {
            throw new AssertionError("unexpected", e);
        }
        check("ab".codePointAt(0) == 'a', "in-bounds codePointAt fine");
        check(new IndexOutOfBoundsException("m").getStackTrace().length > 0, "stack trace captured at construction");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrixDeep();
        testCauseContractDeep();
        testSuppressedContractDeep();
        testStackTraceContractDeep();
        testPrintStackTraceContractDeep();
        testSuidAndSerializationDeep();
        testTriggerSources();
        testSubtypeFamily();
        testIntLongCtorMessages();
        testCollectionTriggerSources();
        testCtorExtras();
        testMoreBoundsForms();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
