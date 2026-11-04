/*
 * @test
 * @summary java.util.concurrent.TimeoutException conformance: white-box structure + black-box behavior.
 * @run main TimeoutExceptionTest
 */

import java.io.*;
import java.util.concurrent.*;

public class TimeoutExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.TimeoutException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TimeoutException e = new TimeoutException("m");
        check(e.getMessage().equals("m"), "message");
        check(e instanceof Exception, "instanceof Exception");
        check(new TimeoutException().getMessage() == null, "no-arg null message");
    }

    // ---- Part 3: Throwable-level contract ----
    static void testThrowableContract() {
        TimeoutException e = new TimeoutException("boom");
        check("java.util.concurrent.TimeoutException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getCause() == null, "no cause unless explicitly given");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");
        TimeoutException e0 = new TimeoutException();
        check("java.util.concurrent.TimeoutException".equals(e0.toString()),
              "toString with null message is just the class name");
        check(e0.initCause(new Exception()) == e0, "initCause returns this");
        check(e0.getCause() != null, "initCause sets the cause");
        check(cls("java.util.concurrent.TimeoutException").getConstructors().length == 2,
              "exactly 2 public constructors (locked ctor matrix)");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(TimeoutException.class).getSerialVersionUID()
                  == 1900926677490660714L, "serialVersionUID locked (1900926677490660714)");

        TimeoutException orig = new TimeoutException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TimeoutException copy = (TimeoutException) ois.readObject();
        ois.close();
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");
    }

    // ---- Part 5: integration: thrown from Future.get(timeout) on a pending future ----
    static void testTimeoutSource() throws Exception {
        CompletableFuture<Integer> pending = new CompletableFuture<>();
        boolean got = false;
        try { pending.get(20, TimeUnit.MILLISECONDS); }
        catch (TimeoutException expected) {
            check(expected.getMessage() == null, "Future.get timeout carries null message");
            got = true;
        }
        check(got, "get(timeout) on pending future throws TimeoutException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableContract();
        testSerializationContract();
        testTimeoutSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
