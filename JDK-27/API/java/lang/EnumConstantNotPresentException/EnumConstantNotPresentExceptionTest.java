/*
 * @test
 * @summary java.lang.EnumConstantNotPresentException conformance: white-box structure + black-box behavior.
 * @run main EnumConstantNotPresentExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EnumConstantNotPresentExceptionTest {
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
        Class<?> c = Class.forName("java.lang.EnumConstantNotPresentException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 1, "1 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.EnumConstantNotPresentException");

        { EnumConstantNotPresentException e = new EnumConstantNotPresentException(Thread.State.class, "RUNNING"); check(e.getMessage() != null, "(Class,String) ctor non-null message"); }

        // common Throwable contract
        EnumConstantNotPresentException t = new EnumConstantNotPresentException(Thread.State.class, "RUNNING");
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.EnumConstantNotPresentException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        { EnumConstantNotPresentException e = new EnumConstantNotPresentException(Thread.State.class, "RUNNING"); check(e.enumType() == Thread.State.class, "enumType"); }
        { EnumConstantNotPresentException e = new EnumConstantNotPresentException(Thread.State.class, "RUNNING"); check("RUNNING".equals(e.constantName()), "constantName"); }
    }

    enum Color { RED, GREEN }

    // ==== Contract deepening (lcN1, 2026-11) ====
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testMessageContract() {
        EnumConstantNotPresentException e =
            new EnumConstantNotPresentException(Color.class, "PURPLE");
        check("EnumConstantNotPresentExceptionTest$Color.PURPLE".equals(e.getMessage()),
              "message is <binaryName>.<constantName>, got: " + e.getMessage());
        check(e.toString().equals(
                  "java.lang.EnumConstantNotPresentException: EnumConstantNotPresentExceptionTest$Color.PURPLE"),
              "toString exact");
        check(e.enumType() == Color.class, "enumType() is the Class passed in");
        check("PURPLE".equals(e.constantName()), "constantName() exact");
        check(e.getLocalizedMessage().equals(e.getMessage()), "getLocalizedMessage == getMessage");
        // message uses the binary name (Thread.State -> java.lang.Thread$State)
        EnumConstantNotPresentException e2 =
            new EnumConstantNotPresentException(Thread.State.class, "RUNNING");
        check("java.lang.Thread$State.RUNNING".equals(e2.getMessage()),
              "binary-name form for nested JDK enum");
        check(e2.enumType() == Thread.State.class, "JDK enumType");
        check("RUNNING".equals(e2.constantName()), "JDK constantName");
        // null constantName is permitted; message contains literal "null"
        EnumConstantNotPresentException e3 =
            new EnumConstantNotPresentException(Color.class, null);
        check(e3.constantName() == null, "null constantName stored as null");
        check("EnumConstantNotPresentExceptionTest$Color.null".equals(e3.getMessage()),
              "null constantName renders as literal null in message");
        check(e3.enumType() == Color.class, "enumType still recorded with null name");
        // null enumType is rejected by the constructor (unreported NPE)
        try {
            new EnumConstantNotPresentException(null, "X");
            throw new AssertionError("null enumType: expected NullPointerException");
        } catch (NullPointerException npe) {
            if (MODERN) {
                check("Cannot invoke \"java.lang.Class.getName()\" because \"enumType\" is null"
                      .equals(npe.getMessage()),
                      "null enumType helpful message, got: " + npe.getMessage());
            } else {
                check(npe.getMessage() == null, "null enumType message null on JDK-8");
            }
        }
    }

    static void testTypeHierarchyAndStructure() throws Exception {
        EnumConstantNotPresentException e =
            new EnumConstantNotPresentException(Color.class, "PURPLE");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e instanceof Exception, "instanceof Exception");
        check(e instanceof java.io.Serializable, "instanceof Serializable");
        Class<?> c = EnumConstantNotPresentException.class;
        check(c.getSuperclass() == RuntimeException.class, "superclass RuntimeException");
        check(c.getConstructors().length == 1, "exactly one public constructor (Class,String)");
        check(c.getMethod("enumType").getReturnType() == Class.class, "enumType() returns Class");
        check(c.getMethod("constantName").getReturnType() == String.class,
              "constantName() returns String");
        // The javap signature: (java.lang.Class<? extends java.lang.Enum>, java.lang.String)
        Class<?>[] ps = c.getConstructors()[0].getParameterTypes();
        check(ps.length == 2 && ps[0] == Class.class && ps[1] == String.class,
              "constructor parameter types (Class, String)");
        check(java.io.ObjectStreamClass.lookup(c).getSerialVersionUID() == -6046998521960521108L,
              "serialVersionUID fixed value");
    }

    static void testThrowablePlumbing() {
        EnumConstantNotPresentException e =
            new EnumConstantNotPresentException(Color.class, "PURPLE");
        check(e.getCause() == null, "fresh exception has null cause");
        EnumConstantNotPresentException other =
            new EnumConstantNotPresentException(Color.class, "PINK");
        check(e.initCause(other) == e, "initCause returns this");
        check(e.getCause() == other, "cause is the same instance after initCause");
        try {
            e.initCause(new RuntimeException("x"));
            throw new AssertionError("second initCause: expected IllegalStateException");
        } catch (IllegalStateException ise) {
            passed++;
        }
        check(e.getSuppressed().length == 0, "no suppressed initially");
        e.addSuppressed(other);
        check(e.getSuppressed().length == 1 && e.getSuppressed()[0] == other,
              "addSuppressed/getSuppressed same instance");
        check(e.getStackTrace().length >= 1, "stack trace captured");
        check(e.getStackTrace()[0].getClassName()
                  .equals("EnumConstantNotPresentExceptionTest"),
              "topmost stack frame is this test class");
        check(e.equals(e) && !e.equals(other), "identity equals (no override)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String printed = bos.toString();
        check(printed.contains("java.lang.EnumConstantNotPresentException"),
              "printStackTrace contains class name");
        check(printed.contains("EnumConstantNotPresentExceptionTest$Color.PURPLE"),
              "printStackTrace contains message");
    }

    static void testSerializationRoundTrip() throws Exception {
        EnumConstantNotPresentException e =
            new EnumConstantNotPresentException(Color.class, "PURPLE");
        e.initCause(new IllegalStateException("why"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        java.io.ObjectInputStream oin =
            new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        EnumConstantNotPresentException r =
            (EnumConstantNotPresentException) oin.readObject();
        oin.close();
        check(r instanceof EnumConstantNotPresentException, "round-trip class");
        check("EnumConstantNotPresentExceptionTest$Color.PURPLE".equals(r.getMessage()),
              "round-trip message preserved");
        check(r.enumType() == Color.class, "round-trip enumType preserved");
        check("PURPLE".equals(r.constantName()), "round-trip constantName preserved");
        check(r.getCause() instanceof IllegalStateException
              && "why".equals(r.getCause().getMessage()), "round-trip cause preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMessageContract();
        testTypeHierarchyAndStructure();
        testThrowablePlumbing();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
