/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionException conformance: white-box structure + black-box behavior.
 * @run main RejectedExecutionExceptionTest
 */

import java.io.*;
import java.util.concurrent.*;

public class RejectedExecutionExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.RejectedExecutionException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        hasCtor(c);
        hasCtor(c, String.class);
        hasCtor(c, String.class, Throwable.class);
        hasCtor(c, Throwable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RejectedExecutionException e = new RejectedExecutionException("m");
        check(e.getMessage().equals("m"), "message");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        Throwable cause = new RuntimeException("root");
        RejectedExecutionException e2 = new RejectedExecutionException("x", cause);
        check(e2.getCause() == cause, "cause from (String,Throwable)");
        check(new RejectedExecutionException().getMessage() == null, "no-arg null message");
    }

    // ---- Part 3: null-cause corners + toString contract ----
    static void testNullCauseCorners() {
        IllegalStateException cause = new IllegalStateException("root");
        RejectedExecutionException fromCause = new RejectedExecutionException(cause);
        check(cause.toString().equals(fromCause.getMessage()),
              "(Throwable) message is exactly cause.toString()");

        RejectedExecutionException nullCause = new RejectedExecutionException((Throwable) null);
        check(nullCause.getCause() == null, "((Throwable)null) cause is null");
        check(nullCause.getMessage() == null, "((Throwable)null) message is null");

        RejectedExecutionException msgNullCause = new RejectedExecutionException("m", null);
        check("m".equals(msgNullCause.getMessage()), "(String,null) message kept verbatim");
        check(msgNullCause.getCause() == null, "(String,null) cause is null");

        RejectedExecutionException e0 = new RejectedExecutionException();
        check("java.util.concurrent.RejectedExecutionException".equals(e0.toString()),
              "toString with null message is just the class name");
        RejectedExecutionException e1 = new RejectedExecutionException("boom");
        check("java.util.concurrent.RejectedExecutionException: boom".equals(e1.toString()),
              "toString is ClassName: message");
        check(e1.fillInStackTrace() == e1, "fillInStackTrace returns this");
        check(cls("java.util.concurrent.RejectedExecutionException").getConstructors().length == 4,
              "exactly 4 public constructors (locked ctor matrix)");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(RejectedExecutionException.class).getSerialVersionUID()
                  == -375805702767069545L, "serialVersionUID locked (-375805702767069545)");

        RejectedExecutionException orig = new RejectedExecutionException("m",
            new IllegalStateException("root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        RejectedExecutionException copy = (RejectedExecutionException) ois.readObject();
        ois.close();
        check("m".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() instanceof IllegalStateException, "round-trip cause type");
        check("root".equals(copy.getCause().getMessage()), "round-trip cause message");

        RejectedExecutionException nullCause = new RejectedExecutionException((Throwable) null);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(nullCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        RejectedExecutionException copy2 = (RejectedExecutionException) ois2.readObject();
        ois2.close();
        check(copy2.getCause() == null, "round-trip null cause stays null");
    }

    // ---- Part 5: integration: thrown when submitting to a shutdown executor ----
    static void testRejectionSource() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            exec.shutdown();
            boolean got = false;
            try { exec.execute(() -> { }); }
            catch (RejectedExecutionException expected) {
                check(expected.getMessage() != null
                          && expected.getMessage().startsWith("Task "),
                      "execute after shutdown rejects with a 'Task ...' message");
                got = true;
            }
            check(got, "execute after shutdown throws RejectedExecutionException");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullCauseCorners();
        testSerializationContract();
        testRejectionSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
