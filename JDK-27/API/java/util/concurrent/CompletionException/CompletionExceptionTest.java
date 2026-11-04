/*
 * @test
 * @summary java.util.concurrent.CompletionException conformance: white-box structure + black-box behavior.
 * @run main CompletionExceptionTest
 */

import java.io.*;
import java.util.concurrent.*;

public class CompletionExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.CompletionException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        hasCtor(c, String.class, Throwable.class);
        hasCtor(c, Throwable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Throwable cause = new RuntimeException("root");
        CompletionException e = new CompletionException("m", cause);
        check(e.getMessage().equals("m"), "message");
        check(e.getCause() == cause, "cause");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        CompletionException e2 = new CompletionException(cause);
        check(e2.getCause() == cause, "cause-only ctor");
        check(e2.getMessage() != null, "(Throwable) ctor sets message to cause.toString()");
    }

    // ---- Part 3: null-cause corners + toString contract ----
    static void testNullCauseCorners() {
        IllegalStateException cause = new IllegalStateException("root");
        CompletionException fromCause = new CompletionException(cause);
        check(cause.toString().equals(fromCause.getMessage()),
              "(Throwable) message is exactly cause.toString()");
        check(("java.util.concurrent.CompletionException: " + cause).equals(fromCause.toString()),
              "toString composes class name with cause.toString()");

        CompletionException nullCause = new CompletionException((Throwable) null);
        check(nullCause.getCause() == null, "((Throwable)null) cause is null");
        check(nullCause.getMessage() == null, "((Throwable)null) message is null");

        CompletionException msgNullCause = new CompletionException("m", null);
        check("m".equals(msgNullCause.getMessage()), "(String,null) message kept verbatim");
        check(msgNullCause.getCause() == null, "(String,null) cause is null");

        CompletionException both = new CompletionException("m", cause);
        check(!both.getMessage().equals(cause.toString()),
              "(String,Throwable) message is NOT cause.toString()");
        check(both.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(both.fillInStackTrace() == both, "fillInStackTrace returns this");
        boolean reInit = false;
        try { both.initCause(new Exception()); }
        catch (IllegalStateException expected) { reInit = true; }
        check(reInit, "initCause after ctor-supplied cause throws IllegalStateException");
        check(cls("java.util.concurrent.CompletionException").getConstructors().length == 2,
              "exactly 2 public constructors (locked ctor matrix; no no-arg ctor)");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(CompletionException.class).getSerialVersionUID()
                  == 7830266012832686185L, "serialVersionUID locked (7830266012832686185)");

        CompletionException orig = new CompletionException("m", new IllegalStateException("root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        CompletionException copy = (CompletionException) ois.readObject();
        ois.close();
        check("m".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() instanceof IllegalStateException, "round-trip cause type");
        check("root".equals(copy.getCause().getMessage()), "round-trip cause message");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");

        CompletionException nullCause = new CompletionException((Throwable) null);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(nullCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        CompletionException copy2 = (CompletionException) ois2.readObject();
        ois2.close();
        check(copy2.getCause() == null, "round-trip null cause stays null");
    }

    // ---- Part 5: integration: wrapping source from CompletableFuture ----
    static void testCompletionSource() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        cf.completeExceptionally(new IllegalStateException("boom"));
        boolean got = false;
        try { cf.join(); }
        catch (CompletionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "join wraps the cause");
            got = true;
        }
        check(got, "CompletableFuture.join throws CompletionException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullCauseCorners();
        testSerializationContract();
        testCompletionSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
