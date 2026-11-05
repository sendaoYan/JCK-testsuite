/*
 * @test
 * @summary java.lang.MatchException conformance: white-box structure + black-box behavior.
 * @run main MatchExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MatchExceptionTest {
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
        Class<?> c = Class.forName("java.lang.MatchException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 1, "1 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.MatchException");

        { Exception cause = new Exception("cause"); MatchException e = new MatchException("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }

        // common Throwable contract
        MatchException t = new MatchException("boom", new Exception("cause"));
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set by constructor");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause after ctor-set cause");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.MatchException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ============================================================
    // ---- Part 3: contract deepening (2026-11, appended) ----
    // ============================================================

    // Locked via ObjectStreamClass.lookup on JDK 27.0.1 (declared 0L in source).
    static final long EXPECTED_SVUID = 0L;

    static final RuntimeException ACCESSOR_EX = new ArithmeticException("accessor boom");

    // Record whose accessor always throws: matching a deconstruction pattern against
    // it is the documented real trigger for MatchException (JEP 441/440 semantics).
    record ThrowingAccessor(int x) {
        public int x() { throw ACCESSOR_EX; }
    }

    static int deconstruct(Object o) {
        if (o instanceof ThrowingAccessor(int v)) return v;
        return -1;
    }

    static void testRealTriggerThrowingAccessor() {
        try {
            int r = deconstruct(new ThrowingAccessor(1));
            throw new AssertionError("expected MatchException, got result " + r);
        } catch (MatchException me) {
            check(true, "deconstruction pattern with throwing accessor throws MatchException");
            check(me.getCause() == ACCESSOR_EX, "cause is the identical accessor exception");
            check("java.lang.ArithmeticException: accessor boom".equals(me.getMessage()),
                  "message defaults to cause.toString() (exact)");
            check(me.getStackTrace() != null && me.getStackTrace().length >= 1,
                  "MatchException carries a stack trace");
            check("deconstruct".equals(me.getStackTrace()[0].getMethodName()),
                  "stack points at the pattern-matching method");
            check(me instanceof RuntimeException, "MatchException is a RuntimeException");
        }
    }

    record Plain(String s) { }
    record Pair(int a, String b) { }
    record Holder(Plain inner) { }

    static void testPatternMatchingWithoutException() {
        check(new Plain("z") instanceof Plain(String v) && "z".equals(v),
              "type-compatible deconstruction succeeds");
        Object nul = null;
        check(!(nul instanceof Plain(String v)), "null never matches a pattern, no exception");
        String r = switch ((Object) "str") {
            case Plain(String v) -> "plain";
            case String s -> "string:" + s;
            default -> "other";
        };
        check("string:str".equals(r), "switch type pattern dispatches without MatchException");
        check(new Pair(2, "two") instanceof Pair(int a, String b) && a == 2 && "two".equals(b),
              "two-component record deconstruction extracts both");
        check(new Holder(new Plain("deep")) instanceof Holder(Plain(String inner))
              && "deep".equals(inner), "nested record pattern deconstruction");
        // accessor of a non-matching record type is not invoked
        Object wrong = new ThrowingAccessor(5);
        check(!(wrong instanceof Plain(String v2)), "type mismatch simply does not match");
        check(new Plain("ok").s().equals("ok"), "normal accessor still usable");
    }

    static void testSwitchNullCase() {
        Object o = null;
        String r = switch (o) {
            case null -> "null-case";
            default -> "other";
        };
        check("null-case".equals(r), "explicit null case handles null without MatchException");
    }

    static void testConstructorNullArgs() {
        MatchException me = new MatchException(null, null);
        check(me.getMessage() == null, "null message permitted");
        check(me.getCause() == null, "null cause permitted");
        check(me.toString().equals("java.lang.MatchException"), "toString without message");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.lang.MatchException");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "MatchException is final");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(c);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.getSerialVersionUID() == EXPECTED_SVUID, "serialVersionUID locked at 0L");
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1 && ctors[0].getParameterCount() == 2,
              "exactly one public constructor with 2 parameters");
        check(ctors[0].getParameterTypes()[0] == String.class
              && ctors[0].getParameterTypes()[1] == Throwable.class,
              "constructor signature (String, Throwable)");
    }

    static void testToStringExact() {
        check("java.lang.MatchException: m"
              .equals(new MatchException("m", new Exception("c")).toString()),
              "toString exact");
    }

    static void testSerializationRoundTrip() throws Exception {
        MatchException original = new MatchException("boom", new Exception("c"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois =
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        MatchException copy = (MatchException) ois.readObject();
        check(copy instanceof MatchException, "round-trip type");
        check("boom".equals(copy.getMessage()), "round-trip message");
        check("c".equals(copy.getCause().getMessage()), "round-trip cause");
        check(copy.toString().equals(original.toString()), "round-trip toString");
    }

    static void testRealStackFrame() throws Exception {
        MatchException me = new MatchException("m", null);
        StackTraceElement[] st = me.getStackTrace();
        check(st.length >= 1, "fresh stack trace non-empty");
        check("MatchExceptionTest".equals(st[0].getClassName()), "[0] class exact");
        check("testRealStackFrame".equals(st[0].getMethodName()), "[0] method exact");
        check(Class.forName(st[0].getClassName()) == MatchExceptionTest.class,
              "Class.forName(frame class) round trip");
    }

    static void testSuppressedAndFill() {
        MatchException me = new MatchException("m", null);
        check(me.getSuppressed().length == 0, "no suppressed initially");
        Throwable s = new Exception("sup");
        me.addSuppressed(s);
        check(me.getSuppressed().length == 1 && me.getSuppressed()[0] == s, "suppressed round trip");
        check(me.fillInStackTrace() == me, "fillInStackTrace returns this");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealTriggerThrowingAccessor();
        testPatternMatchingWithoutException();
        testSwitchNullCase();
        testConstructorNullArgs();
        testStructure();
        testToStringExact();
        testSerializationRoundTrip();
        testRealStackFrame();
        testSuppressedAndFill();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
