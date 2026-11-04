/*
 * @test
 * @summary java.util.concurrent.CancellationException conformance: white-box structure + black-box behavior.
 * @run main CancellationExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.concurrent.*;

public class CancellationExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.CancellationException");
        check(c.getSuperclass() == IllegalStateException.class, "extends IllegalStateException");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CancellationException e = new CancellationException("m");
        check(e.getMessage().equals("m"), "message from (String) ctor");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
        CancellationException e0 = new CancellationException();
        check(e0.getMessage() == null, "no-arg ctor null message");
    }

    // ---- Part 3: Throwable-level contract ----
    static void testThrowableContract() {
        CancellationException e = new CancellationException("boom");
        check("java.util.concurrent.CancellationException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getCause() == null, "no cause unless explicitly given");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");
        CancellationException e0 = new CancellationException();
        check("java.util.concurrent.CancellationException".equals(e0.toString()),
              "toString with null message is just the class name");
        IllegalArgumentException cause = new IllegalArgumentException("root");
        check(e0.initCause(cause) == e0, "initCause returns this");
        check(e0.getCause() == cause, "initCause sets the cause");
        check(cls("java.util.concurrent.CancellationException").getConstructors().length == 2,
              "exactly 2 public constructors (locked ctor matrix)");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(CancellationException.class).getSerialVersionUID()
                  == -9202173006928992231L, "serialVersionUID locked (-9202173006928992231)");

        CancellationException orig = new CancellationException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        CancellationException copy = (CancellationException) ois.readObject();
        ois.close();
        check(copyObj(copy), "round-trip type preserved");
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");
    }

    static boolean copyObj(Object o) { return o instanceof CancellationException; }

    // ---- Part 5: integration: thrown from Future.get on a cancelled FutureTask ----
    static void testCancellationSource() throws Exception {
        FutureTask<Integer> ft = new FutureTask<>(() -> 1);
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
        testWhiteBox();
        testBlackBox();
        testThrowableContract();
        testSerializationContract();
        testCancellationSource();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
