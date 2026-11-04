/*
 * @test
 * @summary java.util.concurrent.ExecutionException API conformance: cause and inheritance.
 * @run main ExecutionExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

public class ExecutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutionException() {
        RuntimeException cause = new RuntimeException();
        ExecutionException e = new ExecutionException(cause);
        check(e instanceof Exception, "extends Exception");
        check(e.getCause() == cause, "getCause");
    }

    // ---- white-box: constructor matrix / hierarchy / serializable shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<ExecutionException> c = ExecutionException.class;
        check(c.getSuperclass() == Exception.class, "getSuperclass() is exactly Exception");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(c.getConstructors().length == 2, "exactly 2 public constructors (locked ctor matrix)");
        check(c.getConstructor(String.class, Throwable.class) != null, "(String,Throwable) ctor present");
        check(c.getConstructor(Throwable.class) != null, "(Throwable) ctor present");
        boolean noArg = false;
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            if (k.getParameterTypes().length == 0) noArg = true;
        }
        check(!noArg, "no no-arg ctor in the public matrix");
    }

    // ---- message/cause contract incl. null-cause corners ----
    static void testCauseContract() {
        IllegalStateException cause = new IllegalStateException("root");
        ExecutionException fromCause = new ExecutionException(cause);
        check(fromCause.getCause() == cause, "(Throwable) keeps cause identity");
        check(cause.toString().equals(fromCause.getMessage()),
              "(Throwable) message defaults to cause.toString()");
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
        check("m".equals(both.getMessage()), "(String,Throwable) message is the given String");
        check(both.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(both.fillInStackTrace() == both, "fillInStackTrace returns this");
        check(both.getStackTrace().length > 0, "stack trace captured at construction");
        boolean reInit = false;
        try { both.initCause(new Exception()); }
        catch (IllegalStateException expected) { reInit = true; }
        check(reInit, "initCause after ctor-supplied cause throws IllegalStateException");
    }

    // ---- serialization: serialVersionUID lock + round-trip ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(ExecutionException.class).getSerialVersionUID()
                  == 7830266012832686185L,
              "serialVersionUID locked (7830266012832686185, shared with CompletionException)");

        ExecutionException orig = new ExecutionException("m", new IllegalStateException("root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
        ExecutionException copy = (ExecutionException) ois.readObject();
        ois.close();
        check("m".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() instanceof IllegalStateException, "round-trip cause type");
        check("root".equals(copy.getCause().getMessage()), "round-trip cause message");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");

        ExecutionException nullCause = new ExecutionException("m", null);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(nullCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(
            new ByteArrayInputStream(bos2.toByteArray()));
        ExecutionException copy2 = (ExecutionException) ois2.readObject();
        ois2.close();
        check("m".equals(copy2.getMessage()), "round-trip (String,null) message preserved");
        check(copy2.getCause() == null, "round-trip (String,null) cause stays null");
    }

    // ---- integration: wrapping source from a failed task ----
    static void testExecutionSource() throws Exception {
        java.util.concurrent.FutureTask<Integer> ft = new java.util.concurrent.FutureTask<Integer>(
            new java.util.concurrent.Callable<Integer>() {
                public Integer call() { throw new IllegalStateException("boom"); }
            });
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
        testExecutionException();
        testWhiteBoxStructure();
        testCauseContract();
        testSerializationContract();
        testExecutionSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
