/*
 * @test
 * @summary java.util.concurrent.TimeoutException API conformance: constructors and inheritance.
 * @run main TimeoutExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeoutException;

public class TimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTimeoutException() {
        TimeoutException e = new TimeoutException("m");
        check(e instanceof Exception, "extends Exception");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new TimeoutException().getMessage() == null, "no-arg ctor has null message");
    }

    // ---- white-box: constructor matrix / hierarchy / serializable shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<TimeoutException> c = TimeoutException.class;
        check(c.getSuperclass() == Exception.class, "getSuperclass() is exactly Exception");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(c.getConstructors().length == 2, "exactly 2 public constructors (locked ctor matrix)");
        check(c.getConstructor() != null, "() ctor present");
        check(c.getConstructor(String.class) != null, "(String) ctor present");
    }

    // ---- message/cause/toString contract ----
    static void testThrowableContract() {
        TimeoutException e = new TimeoutException("boom");
        check("boom".equals(e.getMessage()), "getMessage exact retention");
        check("java.util.concurrent.TimeoutException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getCause() == null, "no cause unless explicitly given");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");

        TimeoutException e0 = new TimeoutException();
        check(e0.getMessage() == null, "no-arg ctor message is null");
        check("java.util.concurrent.TimeoutException".equals(e0.toString()),
              "toString with null message is just the class name");
        check(e0.initCause(new Exception()) == e0, "initCause returns this");
        check(e0.getCause() != null, "initCause sets the cause");
    }

    // ---- serialization: serialVersionUID lock + round-trip ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(TimeoutException.class).getSerialVersionUID()
                  == 1900926677490660714L, "serialVersionUID locked (1900926677490660714)");

        TimeoutException orig = new TimeoutException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
        TimeoutException copy = (TimeoutException) ois.readObject();
        ois.close();
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");
    }

    // ---- integration: thrown from Future.get(timeout) on a pending future ----
    static void testTimeoutSource() throws Exception {
        java.util.concurrent.CompletableFuture<Integer> pending =
            new java.util.concurrent.CompletableFuture<Integer>();
        boolean got = false;
        try { pending.get(20, java.util.concurrent.TimeUnit.MILLISECONDS); }
        catch (TimeoutException expected) {
            check(expected.getMessage() == null, "Future.get timeout carries null message");
            got = true;
        }
        check(got, "get(timeout) on pending future throws TimeoutException");
    }

    public static void main(String[] args) throws Exception {
        testTimeoutException();
        testWhiteBoxStructure();
        testThrowableContract();
        testSerializationContract();
        testTimeoutSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
