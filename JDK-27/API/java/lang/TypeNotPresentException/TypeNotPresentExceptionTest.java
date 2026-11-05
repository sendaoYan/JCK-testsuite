/*
 * @test
 * @summary java.lang.TypeNotPresentException conformance: white-box structure + black-box behavior.
 * @run main TypeNotPresentExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TypeNotPresentExceptionTest {
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
        Class<?> c = Class.forName("java.lang.TypeNotPresentException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 1, "1 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.TypeNotPresentException");

        { Exception cause = new Exception("cause"); TypeNotPresentException e = new TypeNotPresentException("jck.Type", cause); check("Type jck.Type not present".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }

        // common Throwable contract
        TypeNotPresentException t = new TypeNotPresentException("jck.Type", new Exception("cause"));
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set by constructor");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause after ctor-set cause");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.TypeNotPresentException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

        { TypeNotPresentException e = new TypeNotPresentException("jck.Type", new Exception("c")); check("jck.Type".equals(e.typeName()), "typeName"); }
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    static void testMessageContract() {
        TypeNotPresentException e =
            new TypeNotPresentException("jck.Missing", new RuntimeException("r"));
        check("Type jck.Missing not present".equals(e.getMessage()),
              "message template \"Type <name> not present\"");
        check(e.toString().equals("java.lang.TypeNotPresentException: Type jck.Missing not present"),
              "toString exact");
        check("jck.Missing".equals(e.typeName()), "typeName exact");
        check(e.getLocalizedMessage().equals(e.getMessage()),
              "getLocalizedMessage == getMessage");
        // null typeName is permitted and renders as literal "null"
        TypeNotPresentException e2 = new TypeNotPresentException(null, null);
        check(e2.typeName() == null, "null typeName stored as null");
        check("Type null not present".equals(e2.getMessage()),
              "null typeName renders as literal null in message");
        check(e2.getCause() == null, "null cause stored as null");
        // empty typeName
        TypeNotPresentException e3 = new TypeNotPresentException("", null);
        check("Type  not present".equals(e3.getMessage()),
              "empty typeName: double space in message");
        check("".equals(e3.typeName()), "empty typeName returned");
    }

    static void testCauseContract() {
        RuntimeException cause = new RuntimeException("boom");
        TypeNotPresentException e = new TypeNotPresentException("a.B", cause);
        check(e.getCause() == cause, "getCause is the same instance passed in");
        // the (String,Throwable) constructor counts as setting the cause: even a
        // null cause locks initCause out with IllegalStateException.
        TypeNotPresentException n = new TypeNotPresentException("a.B", null);
        try {
            n.initCause(cause);
            throw new AssertionError("initCause after ctor: expected IllegalStateException");
        } catch (IllegalStateException ise) {
            check(ise.getMessage() != null
                  && ise.getMessage().startsWith("Can't overwrite cause"),
                  "initCause ISE message prefix: " + ise.getMessage());
        }
        check(n.getCause() == null, "cause still null after rejected initCause");
    }

    static void testTypeHierarchyAndStructure() throws Exception {
        TypeNotPresentException e = new TypeNotPresentException("a.B", null);
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e instanceof Exception, "instanceof Exception");
        check(e instanceof java.io.Serializable, "instanceof Serializable");
        Class<?> c = TypeNotPresentException.class;
        check(c.getSuperclass() == RuntimeException.class, "superclass RuntimeException");
        check(c.getConstructors().length == 1, "exactly one public constructor (String,Throwable)");
        Class<?>[] ps = c.getConstructors()[0].getParameterTypes();
        check(ps.length == 2 && ps[0] == String.class && ps[1] == Throwable.class,
              "constructor parameter types (String, Throwable)");
        check(c.getMethod("typeName").getReturnType() == String.class,
              "typeName() returns String");
        check(c.getMethod("typeName").getParameterTypes().length == 0,
              "typeName() takes no arguments");
        check(java.io.ObjectStreamClass.lookup(c).getSerialVersionUID() == -5101214195716534496L,
              "serialVersionUID fixed value");
    }

    static void testThrowablePlumbing() {
        TypeNotPresentException e =
            new TypeNotPresentException("a.B", new RuntimeException("r"));
        check(e.getSuppressed().length == 0, "no suppressed initially");
        Throwable sup = new IllegalStateException("sup");
        e.addSuppressed(sup);
        check(e.getSuppressed().length == 1 && e.getSuppressed()[0] == sup,
              "addSuppressed/getSuppressed same instance");
        check(e.getStackTrace().length >= 1, "stack trace captured");
        check(e.getStackTrace()[0].getClassName().equals("TypeNotPresentExceptionTest"),
              "topmost stack frame is this test class");
        check(e.equals(e) && !e.equals(new TypeNotPresentException("a.B", null)),
              "identity equals (no override)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String printed = bos.toString();
        check(printed.contains("java.lang.TypeNotPresentException"),
              "printStackTrace contains class name");
        check(printed.contains("Type a.B not present"), "printStackTrace contains message");
        check(printed.contains("Suppressed: java.lang.IllegalStateException: sup"),
              "printStackTrace contains Suppressed: line");
    }

    static void testSerializationRoundTrip() throws Exception {
        TypeNotPresentException e =
            new TypeNotPresentException("ser.Missing", new ClassNotFoundException("cnf"));
        e.addSuppressed(new IllegalStateException("sup"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        java.io.ObjectInputStream oin =
            new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        TypeNotPresentException r = (TypeNotPresentException) oin.readObject();
        oin.close();
        check(r instanceof TypeNotPresentException, "round-trip class");
        check("ser.Missing".equals(r.typeName()), "round-trip typeName preserved");
        check("Type ser.Missing not present".equals(r.getMessage()),
              "round-trip message preserved");
        check(r.getCause() instanceof ClassNotFoundException
              && "cnf".equals(r.getCause().getMessage()), "round-trip cause preserved");
        check(r.getSuppressed().length == 1
              && r.getSuppressed()[0].getMessage().equals("sup"),
              "round-trip suppressed preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMessageContract();
        testCauseContract();
        testTypeHierarchyAndStructure();
        testThrowablePlumbing();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
