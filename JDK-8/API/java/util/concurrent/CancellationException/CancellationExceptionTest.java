/*
 * @test
 * @summary java.util.concurrent.CancellationException API conformance: constructors and inheritance.
 * @run main CancellationExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.CancellationException;

public class CancellationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCancellationException() {
        CancellationException e = new CancellationException("m");
        check(e instanceof IllegalStateException, "extends IllegalStateException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    // ---- white-box: constructor matrix / hierarchy / serializable shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<CancellationException> c = CancellationException.class;
        check(c.getSuperclass() == IllegalStateException.class,
              "getSuperclass() is exactly IllegalStateException");
        check(RuntimeException.class.isAssignableFrom(c), "unchecked (RuntimeException family)");
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(c.getConstructors().length == 2, "exactly 2 public constructors (locked ctor matrix)");
        check(c.getConstructor() != null, "() ctor present");
        check(c.getConstructor(String.class) != null, "(String) ctor present");
    }

    // ---- message/cause/toString/fillInStackTrace contract ----
    static void testThrowableContract() {
        CancellationException e = new CancellationException("boom");
        check("boom".equals(e.getMessage()), "getMessage exact retention");
        check("java.util.concurrent.CancellationException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getCause() == null, "no cause unless explicitly given");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");

        CancellationException e0 = new CancellationException();
        check(e0.getMessage() == null, "no-arg ctor message is null");
        check("java.util.concurrent.CancellationException".equals(e0.toString()),
              "toString with null message is just the class name");
        IllegalArgumentException cause = new IllegalArgumentException("root");
        check(e0.initCause(cause) == e0, "initCause returns this");
        check(e0.getCause() == cause, "initCause sets the cause");
    }

    // ---- serialization: serialVersionUID lock + round-trip ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(CancellationException.class).getSerialVersionUID()
                  == -9202173006928992231L, "serialVersionUID locked (-9202173006928992231)");

        CancellationException orig = new CancellationException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
        Object copyObj = ois.readObject();
        ois.close();
        check(copyObj instanceof CancellationException, "round-trip type preserved");
        CancellationException copy = (CancellationException) copyObj;
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");
    }

    // ---- integration: thrown from Future.get on a cancelled FutureTask ----
    static void testCancellationSource() throws Exception {
        java.util.concurrent.FutureTask<Integer> ft = new java.util.concurrent.FutureTask<Integer>(
            new java.util.concurrent.Callable<Integer>() {
                public Integer call() { return 1; }
            });
        check(ft.cancel(false), "cancel before run succeeds");
        boolean got = false;
        try { ft.get(); }
        catch (CancellationException expected) {
            check(expected.getMessage() == null, "FutureTask cancellation has null message");
            got = true;
        }
        check(got, "Future.get on cancelled throws CancellationException");
    }

    public static void main(String[] args) throws Exception {
        testCancellationException();
        testWhiteBoxStructure();
        testThrowableContract();
        testSerializationContract();
        testCancellationSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
