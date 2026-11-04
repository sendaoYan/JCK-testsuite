/*
 * @test
 * @summary java.util.concurrent.ExecutionException conformance: white-box structure + black-box behavior.
 * @run main ExecutionExceptionTest
 */

import java.io.*;
import java.util.concurrent.*;

public class ExecutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasCtor(Class<?> c, Class<?>... p) {
        try { c.getConstructor(p); } catch (NoSuchMethodException e) { throw new AssertionError("missing ctor on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ExecutionException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        hasCtor(c, String.class, Throwable.class);
        hasCtor(c, Throwable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Throwable cause = new RuntimeException("root");
        ExecutionException e = new ExecutionException("m", cause);
        check(e.getMessage().equals("m"), "message");
        check(e.getCause() == cause, "cause");
        check(e instanceof Exception, "instanceof Exception");
        ExecutionException e2 = new ExecutionException(cause);
        check(e2.getCause() == cause, "cause-only ctor");
    }

    // ---- Part 3: null-cause corners + toString contract ----
    static void testNullCauseCorners() {
        IllegalStateException cause = new IllegalStateException("root");
        ExecutionException fromCause = new ExecutionException(cause);
        check(cause.toString().equals(fromCause.getMessage()),
              "(Throwable) message is exactly cause.toString()");
        check(("java.util.concurrent.ExecutionException: " + cause).equals(fromCause.toString()),
              "toString composes class name with cause.toString()");

        // documented corner: (String, null) keeps the message and yields a null cause
        ExecutionException msgNullCause = new ExecutionException("m", null);
        check("m".equals(msgNullCause.getMessage()), "(String,null) message kept verbatim");
        check(msgNullCause.getCause() == null, "(String,null) cause is null");

        ExecutionException nullCause = new ExecutionException((Throwable) null);
        check(nullCause.getCause() == null, "((Throwable)null) cause is null");
        check(nullCause.getMessage() == null, "((Throwable)null) message is null");

        ExecutionException both = new ExecutionException("m", cause);
        check(both.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(both.fillInStackTrace() == both, "fillInStackTrace returns this");
        boolean reInit = false;
        try { both.initCause(new Exception()); }
        catch (IllegalStateException expected) { reInit = true; }
        check(reInit, "initCause after ctor-supplied cause throws IllegalStateException");
        check(cls("java.util.concurrent.ExecutionException").getConstructors().length == 2,
              "exactly 2 public constructors (locked ctor matrix; no no-arg ctor)");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(ExecutionException.class).getSerialVersionUID()
                  == 7830266012832686185L,
              "serialVersionUID locked (7830266012832686185, shared with CompletionException)");

        ExecutionException orig = new ExecutionException("m", new IllegalStateException("root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ExecutionException copy = (ExecutionException) ois.readObject();
        ois.close();
        check("m".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() instanceof IllegalStateException, "round-trip cause type");
        check("root".equals(copy.getCause().getMessage()), "round-trip cause message");

        ExecutionException nullCause = new ExecutionException("m", null);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(nullCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        ExecutionException copy2 = (ExecutionException) ois2.readObject();
        ois2.close();
        check("m".equals(copy2.getMessage()), "round-trip (String,null) message preserved");
        check(copy2.getCause() == null, "round-trip (String,null) cause stays null");
    }

    // ---- Part 5: integration: wrapping source from a failed task ----
    static void testExecutionSource() throws Exception {
        FutureTask<Integer> ft = new FutureTask<>(() -> { throw new IllegalStateException("boom"); });
        ft.run();
        boolean got = false;
        try { ft.get(); }
        catch (ExecutionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "get wraps the task failure");
            check("boom".equals(expected.getCause().getMessage()), "cause is the original exception");
            got = true;
        }
        check(got, "Future.get on failed task throws ExecutionException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullCauseCorners();
        testSerializationContract();
        testExecutionSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
