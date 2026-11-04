/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionException API conformance: constructors and inheritance.
 * @run main RejectedExecutionExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.RejectedExecutionException;

public class RejectedExecutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRejectedExecutionException() {
        RejectedExecutionException e = new RejectedExecutionException("m");
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    // ---- white-box: constructor matrix / hierarchy / serializable shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<RejectedExecutionException> c = RejectedExecutionException.class;
        check(c.getSuperclass() == RuntimeException.class, "getSuperclass() is exactly RuntimeException");
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(c.getConstructors().length == 4, "exactly 4 public constructors (locked ctor matrix)");
        check(c.getConstructor() != null, "() ctor present");
        check(c.getConstructor(String.class) != null, "(String) ctor present");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) ctor present");
        check(c.getConstructor(Throwable.class) != null, "(Throwable) ctor present");
    }

    // ---- message/cause contract incl. null-cause corners ----
    static void testCauseContract() {
        IllegalStateException cause = new IllegalStateException("root");
        RejectedExecutionException fromCause = new RejectedExecutionException(cause);
        check(fromCause.getCause() == cause, "(Throwable) keeps cause identity");
        check(cause.toString().equals(fromCause.getMessage()),
              "(Throwable) message defaults to cause.toString()");

        RejectedExecutionException nullCause = new RejectedExecutionException((Throwable) null);
        check(nullCause.getCause() == null, "((Throwable)null) cause is null");
        check(nullCause.getMessage() == null, "((Throwable)null) message is null");

        RejectedExecutionException msgNullCause = new RejectedExecutionException("m", null);
        check("m".equals(msgNullCause.getMessage()), "(String,null) message kept verbatim");
        check(msgNullCause.getCause() == null, "(String,null) cause is null");

        RejectedExecutionException e0 = new RejectedExecutionException();
        check(e0.getMessage() == null, "no-arg ctor message is null");
        check("java.util.concurrent.RejectedExecutionException".equals(e0.toString()),
              "toString with null message is just the class name");
        RejectedExecutionException e1 = new RejectedExecutionException("boom");
        check("java.util.concurrent.RejectedExecutionException: boom".equals(e1.toString()),
              "toString is ClassName: message");
        check(e1.fillInStackTrace() == e1, "fillInStackTrace returns this");
        check(e1.getSuppressed().length == 0, "no suppressed exceptions initially");
    }

    // ---- serialization: serialVersionUID lock + round-trip ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(RejectedExecutionException.class).getSerialVersionUID()
                  == -375805702767069545L, "serialVersionUID locked (-375805702767069545)");

        RejectedExecutionException orig = new RejectedExecutionException("m",
            new IllegalStateException("root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
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
        ObjectInputStream ois2 = new ObjectInputStream(
            new ByteArrayInputStream(bos2.toByteArray()));
        RejectedExecutionException copy2 = (RejectedExecutionException) ois2.readObject();
        ois2.close();
        check(copy2.getCause() == null, "round-trip null cause stays null");
    }

    // ---- integration: thrown when submitting to a shutdown executor ----
    static void testRejectionSource() throws Exception {
        java.util.concurrent.ExecutorService exec =
            java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            exec.shutdown();
            boolean got = false;
            try { exec.execute(new Runnable() { public void run() { } }); }
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
        testRejectedExecutionException();
        testWhiteBoxStructure();
        testCauseContract();
        testSerializationContract();
        testRejectionSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
