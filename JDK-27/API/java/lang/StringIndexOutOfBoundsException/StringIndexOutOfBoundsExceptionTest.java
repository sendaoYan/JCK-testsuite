/*
 * @test
 * @summary java.lang.StringIndexOutOfBoundsException conformance: white-box structure + black-box behavior.
 * @run main StringIndexOutOfBoundsExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringIndexOutOfBoundsExceptionTest {
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
        Class<?> c = Class.forName("java.lang.StringIndexOutOfBoundsException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == IndexOutOfBoundsException.class, "superclass is IndexOutOfBoundsException");
        check(c.getDeclaredConstructors().length == 3, "3 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StringIndexOutOfBoundsException");

        StringIndexOutOfBoundsException e0 = new StringIndexOutOfBoundsException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException(5); check(e.getMessage() != null, "(int) ctor non-null message"); }

        // common Throwable contract
        StringIndexOutOfBoundsException t = new StringIndexOutOfBoundsException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.StringIndexOutOfBoundsException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        // trigger: string index out of range
        expect(StringIndexOutOfBoundsException.class, () -> { "abc".charAt(99); }, "trigger string index out of range");
    }

    // ===================== contract deepening (2026-11) =====================

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testHierarchyDeep() {
        Class<?> c = StringIndexOutOfBoundsException.class;
        check(c.getSuperclass() == IndexOutOfBoundsException.class, "direct superclass is IndexOutOfBoundsException");
        check(RuntimeException.class.isAssignableFrom(c), "assignable to RuntimeException");
        check(Exception.class.isAssignableFrom(c), "assignable to Exception");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        check(!ArrayIndexOutOfBoundsException.class.isAssignableFrom(c),
              "sibling of ArrayIndexOutOfBoundsException, not a subtype");
        check(c.getConstructors().length == 3, "exactly three public constructors: (), (String), (int)");
    }

    static void testIntCtorDirect() {
        // The (int) constructor exists on both releases (confirmed via javap) and its
        // message format is byte-identical on JDK 8 and JDK 27: lock it without branching.
        check("String index out of range: 5".equals(new StringIndexOutOfBoundsException(5).getMessage()),
              "(int) ctor message for 5");
        check("String index out of range: 0".equals(new StringIndexOutOfBoundsException(0).getMessage()),
              "(int) ctor message for 0");
        check("String index out of range: -3".equals(new StringIndexOutOfBoundsException(-3).getMessage()),
              "(int) ctor message for a negative index");
        check("String index out of range: -2147483648".equals(
                  new StringIndexOutOfBoundsException(Integer.MIN_VALUE).getMessage()),
              "(int) ctor message for Integer.MIN_VALUE");
        check(new StringIndexOutOfBoundsException(7) instanceof IndexOutOfBoundsException,
              "(int) ctor result is an IndexOutOfBoundsException");
    }

    static void testStringCtorDeep() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException("index 9 too big");
        check("index 9 too big".equals(e.getMessage()), "(String) ctor message verbatim");
        check(e.getCause() == null, "(String) ctor leaves cause null");
        check(e.toString().equals("java.lang.StringIndexOutOfBoundsException: index 9 too big"),
              "toString is ClassName: message");
    }

    static void testSuppressed() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException("m");
        check(e.getSuppressed().length == 0, "fresh exception has an empty suppressed array");
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        Throwable[] sup = e.getSuppressed();
        check(sup.length == 2 && sup[0] == s1 && sup[1] == s2, "addSuppressed keeps order and identity");
        sup[0] = null; // try to mutate the returned array
        check(e.getSuppressed()[0] == s1, "getSuppressed returns a defensive copy");
        try {
            e.addSuppressed(null);
            throw new AssertionError("addSuppressed(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check("Cannot suppress a null exception.".equals(expected.getMessage()),
                  "addSuppressed(null) message identical on JDK 8 and JDK 27");
        }
    }

    static void testCauseChain() {
        StringIndexOutOfBoundsException outer = new StringIndexOutOfBoundsException("outer");
        RuntimeException root = new RuntimeException("root");
        check(outer.initCause(root) == outer, "initCause returns this");
        check(outer.getCause() == root, "initCause stores the cause");
        try {
            outer.initCause(new Exception("again"));
            throw new AssertionError("second initCause: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            check(true, "second initCause throws IllegalStateException");
        }
        Exception wrapper = new Exception("w", outer);
        check(wrapper.getCause() == outer, "Throwable(String,Throwable) wrapping pattern wires the cause");
    }

    static void testStackTracePrinting() {
        StringIndexOutOfBoundsException e = new StringIndexOutOfBoundsException("boom");
        check(e.getStackTrace().length >= 1, "stack trace captured at construction");
        check(e.getStackTrace()[0].getClassName().equals(StringIndexOutOfBoundsExceptionTest.class.getName()),
              "top frame is this test class");
        check("boom".equals(e.getLocalizedMessage()), "getLocalizedMessage delegates to getMessage");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String out = bos.toString();
        check(out.contains("java.lang.StringIndexOutOfBoundsException: boom"),
              "printStackTrace prints the class and message");
        check(out.contains("at " + StringIndexOutOfBoundsExceptionTest.class.getName() + ".testStackTracePrinting"),
              "printStackTrace prints frames");
        e.addSuppressed(new Exception("hidden"));
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos2));
        check(bos2.toString().contains("Suppressed: java.lang.Exception: hidden"),
              "printStackTrace prints the Suppressed section");
    }

    static void testRealTriggersExact() {
        // Real API violations raise exactly this class; message text is release-specific.
        try {
            "abc".charAt(5);
            throw new AssertionError("String.charAt(5): expected SIOOBE");
        } catch (StringIndexOutOfBoundsException e) {
            check(MODERN ? "Index 5 out of bounds for length 3".equals(e.getMessage())
                         : "String index out of range: 5".equals(e.getMessage()),
                  "String.charAt(5) message locked per release");
        }
        try {
            "abc".substring(5);
            throw new AssertionError("String.substring(5): expected SIOOBE");
        } catch (StringIndexOutOfBoundsException e) {
            check(MODERN ? "Range [5, 3) out of bounds for length 3".equals(e.getMessage())
                         : "String index out of range: -2".equals(e.getMessage()),
                  "String.substring(5) message locked per release");
        }
        try {
            new StringBuilder("abc").charAt(-1);
            throw new AssertionError("StringBuilder.charAt(-1): expected SIOOBE");
        } catch (StringIndexOutOfBoundsException e) {
            check(MODERN ? "Index -1 out of bounds for length 3".equals(e.getMessage())
                         : "String index out of range: -1".equals(e.getMessage()),
                  "StringBuilder.charAt(-1) message locked per release");
        }
        try {
            new StringBuffer("abc").substring(4);
            throw new AssertionError("StringBuffer.substring(4): expected SIOOBE");
        } catch (StringIndexOutOfBoundsException e) {
            check(MODERN ? "Range [4, 3) out of bounds for length 3".equals(e.getMessage())
                         : "String index out of range: -1".equals(e.getMessage()),
                  "StringBuffer.substring(4) message locked per release");
        }
        try {
            CharSequence cs = "abc";
            cs.charAt(7);
            throw new AssertionError("CharSequence.charAt(7): expected SIOOBE");
        } catch (StringIndexOutOfBoundsException e) {
            check(e instanceof IndexOutOfBoundsException, "CharSequence view violation is an IOOBE");
        }
        try {
            "abc".charAt(99);
            throw new AssertionError("charAt(99): expected an exception");
        } catch (IndexOutOfBoundsException e) {
            check(e.getClass() == StringIndexOutOfBoundsException.class,
                  "SIOOBE is catchable as its supertype IndexOutOfBoundsException");
        }
        StringIndexOutOfBoundsException chained = new StringIndexOutOfBoundsException("chain");
        RuntimeException why = new RuntimeException("why");
        chained.initCause(why);
        check(chained.getCause() == why && "chain".equals(chained.getMessage()),
              "SIOOBE participates in cause chains like any Throwable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchyDeep();
        testIntCtorDirect();
        testStringCtorDeep();
        testSuppressed();
        testCauseChain();
        testStackTracePrinting();
        testRealTriggersExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
