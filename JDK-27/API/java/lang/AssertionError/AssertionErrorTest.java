/*
 * @test
 * @summary java.lang.AssertionError conformance: white-box structure + black-box behavior.
 * @run main AssertionErrorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AssertionErrorTest {
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
        Class<?> c = Class.forName("java.lang.AssertionError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(c.getConstructors().length == 9, "9 public constructors (10 declared incl. private String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.AssertionError");

        AssertionError e0 = new AssertionError();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { AssertionError e = new AssertionError("obj"); check(e.getMessage() != null, "(Object) ctor non-null message"); }
        { AssertionError e = new AssertionError(true); check(e.getMessage() != null, "(boolean) ctor non-null message"); }
        { AssertionError e = new AssertionError('x'); check(e.getMessage() != null, "(char) ctor non-null message"); }
        { AssertionError e = new AssertionError(7); check(e.getMessage() != null, "(int) ctor non-null message"); }
        { AssertionError e = new AssertionError(7L); check(e.getMessage() != null, "(long) ctor non-null message"); }
        { AssertionError e = new AssertionError(1.5f); check(e.getMessage() != null, "(float) ctor non-null message"); }
        { AssertionError e = new AssertionError(2.5); check(e.getMessage() != null, "(double) ctor non-null message"); }
        { Exception cause = new Exception("cause"); AssertionError e = new AssertionError("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }

        // common Throwable contract
        AssertionError t = new AssertionError();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.AssertionError"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ---- Deepening (lcO1, 2026-11): AssertionError contract matrix ----
    static void testPrimitiveMessagesExact() {
        check(new AssertionError(true).getMessage().equals("true"), "(boolean true) message exact");
        check(new AssertionError(false).getMessage().equals("false"), "(boolean false) message exact");
        check(new AssertionError('x').getMessage().equals("x"), "(char) message exact");
        check(new AssertionError('\u0000').getMessage().equals("\u0000"), "(char NUL) message is NUL char");
        check(new AssertionError(42).getMessage().equals("42"), "(int) message exact");
        check(new AssertionError(0).getMessage().equals("0"), "(int zero) message exact");
        check(new AssertionError(Integer.MAX_VALUE).getMessage().equals("2147483647"), "(int MAX) message exact");
        check(new AssertionError(Integer.MIN_VALUE).getMessage().equals("-2147483648"), "(int MIN) message exact");
        check(new AssertionError(42L).getMessage().equals("42"), "(long) message exact");
        check(new AssertionError(Long.MAX_VALUE).getMessage().equals("9223372036854775807"),
              "(long MAX) message exact");
        check(new AssertionError(Long.MIN_VALUE).getMessage().equals("-9223372036854775808"),
              "(long MIN) message exact");
        check(new AssertionError(1.5f).getMessage().equals("1.5"), "(float) message exact");
        check(new AssertionError(0.1f).getMessage().equals("0.1"), "(float 0.1) message exact");
        check(new AssertionError(-0.0f).getMessage().equals("-0.0"), "(float -0.0) message exact");
        check(new AssertionError(Float.NaN).getMessage().equals("NaN"), "(float NaN) message exact");
        check(new AssertionError(Float.POSITIVE_INFINITY).getMessage().equals("Infinity"),
              "(float +Inf) message exact");
        check(new AssertionError(Float.NEGATIVE_INFINITY).getMessage().equals("-Infinity"),
              "(float -Inf) message exact");
        check(new AssertionError(1.5).getMessage().equals("1.5"), "(double) message exact");
        check(new AssertionError(0.1).getMessage().equals("0.1"), "(double 0.1) message exact");
        check(new AssertionError(-0.0).getMessage().equals("-0.0"), "(double -0.0) message exact");
        check(new AssertionError(Double.NaN).getMessage().equals("NaN"), "(double NaN) message exact");
        check(new AssertionError(Double.POSITIVE_INFINITY).getMessage().equals("Infinity"),
              "(double +Inf) message exact");
        check(new AssertionError(Double.NEGATIVE_INFINITY).getMessage().equals("-Infinity"),
              "(double -Inf) message exact");
    }

    static void testObjectAndCauseCtor() {
        check(new AssertionError((Object) "boom").getMessage().equals("boom"), "(Object String) message exact");
        check(new AssertionError((Object) null).getMessage().equals("null"),
              "(Object null) message is the literal \"null\"");
        check(new AssertionError((Object) Integer.valueOf(42)).getMessage().equals("42"),
              "(Object Integer) message via toString");
        check(new AssertionError((Object) Boolean.TRUE).getMessage().equals("true"),
              "(Object Boolean) message via toString");
        check(new AssertionError((Object) new StringBuilder("sb")).getMessage().equals("sb"),
              "(Object StringBuilder) message via toString");
        Throwable cause = new IllegalStateException("root");
        AssertionError e = new AssertionError("boom", cause);
        check("boom".equals(e.getMessage()), "(String,Throwable) message kept");
        check(e.getCause() == cause, "(String,Throwable) cause identity");
        AssertionError nullMsg = new AssertionError((String) null, cause);
        check(nullMsg.getMessage() == null && nullMsg.getCause() == cause,
              "(null,Throwable) keeps null message and cause");
        try {
            e.initCause(new Exception("other"));
            throw new AssertionError("unreachable: initCause after ctor-set cause");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new AssertionError().getCause() == null, "fresh cause null");
        AssertionError nullCause = new AssertionError("m", (Throwable) null);
        check(nullCause.getCause() == null, "(m,null) cause reads null");
        try {
            nullCause.initCause(cause);
            throw new AssertionError("unreachable: initCause after (String,Throwable) ctor");
        } catch (IllegalStateException expected2) {
            passed++; // (String,Throwable) ctor pins the cause slot even for a null cause
        }
    }

    static void testToStringMatrix() {
        check(new AssertionError().toString().equals("java.lang.AssertionError"), "toString bare");
        check(new AssertionError("boom").toString().equals("java.lang.AssertionError: boom"), "toString with message");
        check(new AssertionError(42).toString().equals("java.lang.AssertionError: 42"), "toString int form");
        check(new AssertionError('x').toString().equals("java.lang.AssertionError: x"), "toString char form");
        check(new AssertionError(true).toString().equals("java.lang.AssertionError: true"), "toString boolean form");
        check(new AssertionError((Object) null).toString().equals("java.lang.AssertionError: null"),
              "toString Object-null form");
        check(new AssertionError().getLocalizedMessage() == null, "localized null when message null");
        check(new AssertionError("m").getLocalizedMessage().equals("m"), "localized equals message");
    }

    static void testReflectionCtorSet() throws Exception {
        Class<?> c = AssertionError.class;
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 9, "exactly 9 public constructors");
        check(c.getConstructor() != null, "no-arg public");
        check(c.getConstructor(Object.class) != null, "(Object) public");
        check(c.getConstructor(boolean.class) != null, "(boolean) public");
        check(c.getConstructor(char.class) != null, "(char) public");
        check(c.getConstructor(int.class) != null, "(int) public");
        check(c.getConstructor(long.class) != null, "(long) public");
        check(c.getConstructor(float.class) != null, "(float) public");
        check(c.getConstructor(double.class) != null, "(double) public");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) public");
        try {
            c.getConstructor(String.class);
            throw new AssertionError("unreachable: public (String) must not exist");
        } catch (NoSuchMethodException expected2) {
            passed++; // the (String) ctor is private, used internally
        }
        try {
            c.getConstructor(Throwable.class);
            throw new AssertionError("unreachable: public (Throwable) must not exist");
        } catch (NoSuchMethodException expected3) {
            passed++;
        }
    }

    static void testCommonThrowableContract() {
        AssertionError t = new AssertionError("boom");
        check(t.getSuppressed().length == 0, "suppressed empty by default");
        IllegalStateException sup = new IllegalStateException("sup");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        try {
            t.addSuppressed(null);
            throw new AssertionError("unreachable: addSuppressed(null)");
        } catch (NullPointerException e) {
            check("Cannot suppress a null exception.".equals(e.getMessage()), "addSuppressed(null) NPE message exact");
        }
        try {
            AssertionError self = new AssertionError("s");
            self.addSuppressed(self);
            throw new AssertionError("unreachable: addSuppressed(self)");
        } catch (IllegalArgumentException e) {
            check("Self-suppression not permitted".equals(e.getMessage()), "addSuppressed(self) IAE message exact");
        }
        try {
            AssertionError self = new AssertionError("s");
            self.initCause(self);
            throw new AssertionError("unreachable: initCause(self)");
        } catch (IllegalArgumentException e) {
            check("Self-causation not permitted".equals(e.getMessage()), "initCause(self) IAE message exact");
        }
        StackTraceElement[] st = t.getStackTrace();
        check(st.length > 0, "stack trace captured");
        check(st[0].getClassName().equals("AssertionErrorTest") && st[0].getMethodName().equals("testCommonThrowableContract"),
              "top frame class+method exact");
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("C", "m", "F.java", 1) });
        check(t.getStackTrace().length == 1 && t.getStackTrace()[0].getClassName().equals("C"), "setStackTrace roundtrip");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        check(t.getStackTrace()[0].getClassName().equals("AssertionErrorTest"), "fillInStackTrace re-anchors top frame");
    }

    static void testPrintStackTraceContract() {
        AssertionError t = new AssertionError("boom");
        t.addSuppressed(new IllegalStateException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintStream(bos));
        String out = new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        check(out.startsWith("java.lang.AssertionError: boom"), "first line is toString");
        check(out.contains("\tat "), "frame lines present");
        check(out.contains("Suppressed: java.lang.IllegalStateException: sup"), "Suppressed section present");
        check(!out.contains("Caused by"), "no Caused by without a cause");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pw = new java.io.PrintWriter(bos2);
        t.printStackTrace(pw);
        pw.flush();
        check(new String(bos2.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).startsWith("java.lang.AssertionError"),
              "PrintWriter overload works");
    }

    static void testSuidAndSerializable() {
        check(java.io.ObjectStreamClass.lookup(AssertionError.class).getSerialVersionUID() == -5013299493970297370L,
              "serialVersionUID locked via ObjectStreamClass (same on JDK-8 and JDK-27)");
        check(new AssertionError("m") instanceof java.io.Serializable, "AssertionError is Serializable");
        try {
            AssertionError t = new AssertionError("ser");
            t.addSuppressed(new IllegalStateException("sup"));
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.close();
            AssertionError copy = (AssertionError) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check("ser".equals(copy.getMessage()), "round-trip keeps message");
            check(copy.getSuppressed().length == 1 && "sup".equals(copy.getSuppressed()[0].getMessage()),
                  "round-trip keeps suppressed");
            check(copy.toString().equals("java.lang.AssertionError: ser"), "round-trip toString exact");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void testHierarchyExtra() {
        check(new AssertionError() instanceof Error, "is an Error");
        check(new AssertionError() instanceof Throwable, "is a Throwable");
        check(!Exception.class.isAssignableFrom(AssertionError.class), "is not an Exception");
        check(AssertionError.class.getSuperclass() == Error.class, "superclass is Error");
        check(!java.lang.reflect.Modifier.isFinal(AssertionError.class.getModifiers()), "subclassable");
    }

    static void testMoreMessageForms() {
        StringBuilder letters = new StringBuilder();
        for (char ch = 'a'; ch <= 'e'; ch++) letters.append(new AssertionError(ch).getMessage());
        check(letters.toString().equals("abcde"), "(char) a..e messages concatenate");
        check(new AssertionError(-1).getMessage().equals("-1"), "(int -1) message exact");
        check(new AssertionError(-42).getMessage().equals("-42"), "(int -42) message exact");
        check(new AssertionError(0L).getMessage().equals("0"), "(long zero) message exact");
        check(new AssertionError(-9L).getMessage().equals("-9"), "(long -9) message exact");
        check(new AssertionError(0.0f).getMessage().equals("0.0"), "(float 0.0) message exact");
        check(new AssertionError(-1.5f).getMessage().equals("-1.5"), "(float -1.5) message exact");
        check(new AssertionError(0.0).getMessage().equals("0.0"), "(double 0.0) message exact");
        check(new AssertionError(100.0).getMessage().equals("100.0"), "(double 100.0) message exact");
        check(new AssertionError((Object) "").getMessage().equals(""), "(Object empty String) message empty");
        check(new AssertionError((Object) Character.valueOf('z')).getMessage().equals("z"),
              "(Object Character) message via toString");
        check(new AssertionError((Object) Double.valueOf(1.5)).getMessage().equals("1.5"),
              "(Object Double) message via toString");
    }

    static void throwAssertion() {
        throw new AssertionError("thrown-directly");
    }

    static void testCatchSemantics() {
        boolean caughtAsError = false;
        try {
            throwAssertion();
        } catch (Error e) {
            caughtAsError = "thrown-directly".equals(e.getMessage());
        }
        check(caughtAsError, "AssertionError is caught by catch (Error)");
        boolean caughtAsThrowable = false;
        try {
            throwAssertion();
        } catch (Throwable t) {
            caughtAsThrowable = t instanceof AssertionError;
        }
        check(caughtAsThrowable, "AssertionError is caught by catch (Throwable)");
        boolean leaked = false;
        try {
            try {
                throwAssertion();
            } catch (Exception e) {
                leaked = true; // must NOT happen: AE is an Error, not an Exception
            }
        } catch (AssertionError expected) {
            passed++;
        }
        check(!leaked, "AssertionError is not caught by catch (Exception)");
    }

    static void testAssertSemanticsProbe() {
        boolean ea = false;
        assert ea = true;
        if (!ea) {
            check(true, "without -ea the assert expression is not evaluated");
            boolean flag = false;
            assert flag = true;
            check(!flag, "assert side effect skipped when disabled");
        } else {
            boolean flag = false;
            assert flag = true;
            check(flag, "assert expression evaluated when enabled");
            try {
                assert false;
                throw new AssertionError("unreachable");
            } catch (AssertionError e) {
                check(e.getMessage() == null, "bare assert false has null message");
            }
        }
    }

    static void testOverloadDispatch() {
        // The primitive overloads and the (Object) overload produce the same text
        // for boxed values, but they are distinct constructors.
        check(new AssertionError(42).getMessage().equals(new AssertionError((Object) Integer.valueOf(42)).getMessage()),
              "(int) and (Object Integer) agree");
        check(new AssertionError('x').getMessage().equals(new AssertionError((Object) Character.valueOf('x')).getMessage()),
              "(char) and (Object Character) agree");
        check(new AssertionError(true).getMessage().equals(new AssertionError((Object) Boolean.TRUE).getMessage()),
              "(boolean) and (Object Boolean) agree");
        check(new AssertionError(1.5f).getMessage().equals(new AssertionError((Object) Float.valueOf(1.5f)).getMessage()),
              "(float) and (Object Float) agree");
        check(new AssertionError(1.5).getMessage().equals(new AssertionError((Object) Double.valueOf(1.5)).getMessage()),
              "(double) and (Object Double) agree");
        check(new AssertionError(9L).getMessage().equals(new AssertionError((Object) Long.valueOf(9L)).getMessage()),
              "(long) and (Object Long) agree");
        AssertionError a = new AssertionError(42);
        AssertionError b = new AssertionError(42);
        check(a.getMessage().equals(b.getMessage()), "same form gives same message");
        check(a.toString().equals(b.toString()), "same form gives same toString");
        check(a != b, "two constructions are distinct instances (no value equality)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPrimitiveMessagesExact();
        testObjectAndCauseCtor();
        testToStringMatrix();
        testReflectionCtorSet();
        testCommonThrowableContract();
        testPrintStackTraceContract();
        testSuidAndSerializable();
        testHierarchyExtra();
        testMoreMessageForms();
        testCatchSemantics();
        testAssertSemanticsProbe();
        testOverloadDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
