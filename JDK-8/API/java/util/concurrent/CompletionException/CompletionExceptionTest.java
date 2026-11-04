/*
 * @test
 * @summary java.util.concurrent.CompletionException API conformance: cause and inheritance.
 * @run main CompletionExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletionException;

public class CompletionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletionException() {
        RuntimeException cause = new RuntimeException();
        CompletionException e = new CompletionException(cause);
        check(e instanceof RuntimeException, "extends RuntimeException");
        check(e.getCause() == cause, "getCause");

        CompletionException e2 = new CompletionException("m", cause);
        check("m".equals(e2.getMessage()), "message from (String,Throwable) ctor");
        check(e2.getCause() == cause, "cause from (String,Throwable) ctor");
    }

    // ---- white-box: constructor matrix / hierarchy / serializable shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<CompletionException> c = CompletionException.class;
        check(c.getSuperclass() == RuntimeException.class, "getSuperclass() is exactly RuntimeException");
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
        CompletionException fromCause = new CompletionException(cause);
        check(fromCause.getCause() == cause, "(Throwable) keeps cause identity");
        check(cause.toString().equals(fromCause.getMessage()),
              "(Throwable) message defaults to cause.toString()");
        check(("java.util.concurrent.CompletionException: " + cause).equals(fromCause.toString()),
              "toString composes class name with cause.toString()");

        CompletionException nullCause = new CompletionException((Throwable) null);
        check(nullCause.getCause() == null, "((Throwable)null) cause is null");
        check(nullCause.getMessage() == null, "((Throwable)null) message is null");

        CompletionException msgNullCause = new CompletionException("m", null);
        check("m".equals(msgNullCause.getMessage()), "(String,null) message kept verbatim");
        check(msgNullCause.getCause() == null, "(String,null) cause is null, not this");

        CompletionException both = new CompletionException("m", cause);
        check("m".equals(both.getMessage()), "(String,Throwable) message is the given String");
        check(!both.getMessage().equals(cause.toString()),
              "(String,Throwable) message is NOT cause.toString()");
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
        check(ObjectStreamClass.lookup(CompletionException.class).getSerialVersionUID()
                  == 7830266012832686185L, "serialVersionUID locked (7830266012832686185)");

        CompletionException orig = new CompletionException("m", new IllegalStateException("root"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
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
        ObjectInputStream ois2 = new ObjectInputStream(
            new ByteArrayInputStream(bos2.toByteArray()));
        CompletionException copy2 = (CompletionException) ois2.readObject();
        ois2.close();
        check(copy2.getCause() == null, "round-trip null cause stays null");
    }

    // ---- integration: wrapping source from a CompletableFuture ----
    static void testCompletionSource() throws Exception {
        java.util.concurrent.CompletableFuture<Integer> cf = new java.util.concurrent.CompletableFuture<Integer>();
        cf.completeExceptionally(new IllegalStateException("boom"));
        boolean got = false;
        try { cf.join(); }
        catch (CompletionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "join wraps cause");
            got = true;
        }
        check(got, "CompletableFuture.join throws CompletionException");
    }

    public static void main(String[] args) throws Exception {
        testCompletionException();
        testWhiteBoxStructure();
        testCauseContract();
        testSerializationContract();
        testCompletionSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
