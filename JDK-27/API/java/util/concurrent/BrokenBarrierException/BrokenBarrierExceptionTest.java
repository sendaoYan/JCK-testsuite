/*
 * @test
 * @summary java.util.concurrent.BrokenBarrierException conformance: white-box structure + black-box behavior.
 * @run main BrokenBarrierExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.concurrent.*;

public class BrokenBarrierExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.BrokenBarrierException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BrokenBarrierException e = new BrokenBarrierException("m");
        check(e.getMessage().equals("m"), "message from (String) ctor");
        check(e.getCause() == null, "no cause");
        BrokenBarrierException e0 = new BrokenBarrierException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        check(e0 instanceof Exception, "instanceof Exception");
    }

    // ---- Part 3: Throwable-level contract (toString/fillInStackTrace/initCause) ----
    static void testThrowableContract() {
        BrokenBarrierException e = new BrokenBarrierException("boom");
        check("java.util.concurrent.BrokenBarrierException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) honoured");
        BrokenBarrierException e0 = new BrokenBarrierException();
        check("java.util.concurrent.BrokenBarrierException".equals(e0.toString()),
              "toString with null message is just the class name");
        IllegalStateException cause = new IllegalStateException("root");
        check(e0.initCause(cause) == e0, "initCause returns this");
        check(e0.getCause() == cause, "initCause sets the cause");
        boolean secondInit = false;
        try { e0.initCause(new Exception()); }
        catch (IllegalStateException expected) { secondInit = true; }
        check(secondInit, "second initCause throws IllegalStateException");
        check(cls("java.util.concurrent.BrokenBarrierException").getConstructors().length == 2,
              "exactly 2 public constructors (locked ctor matrix)");
        check(java.io.Serializable.class.isAssignableFrom(
                  cls("java.util.concurrent.BrokenBarrierException")), "Serializable via Throwable");
    }

    // ---- Part 4: serialization contract (suid lock + round-trip) ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(BrokenBarrierException.class).getSerialVersionUID()
                  == 7117394618823254244L, "serialVersionUID locked (7117394618823254244)");

        BrokenBarrierException orig = new BrokenBarrierException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BrokenBarrierException copy = (BrokenBarrierException) ois.readObject();
        ois.close();
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");

        BrokenBarrierException withCause = new BrokenBarrierException("wrapped");
        withCause.initCause(new IllegalStateException("root"));
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(withCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        BrokenBarrierException copy2 = (BrokenBarrierException) ois2.readObject();
        ois2.close();
        check("wrapped".equals(copy2.getMessage()), "round-trip message with cause");
        check(copy2.getCause() instanceof IllegalStateException, "round-trip cause type");
        check("root".equals(copy2.getCause().getMessage()), "round-trip cause message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableContract();
        testSerializationContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
