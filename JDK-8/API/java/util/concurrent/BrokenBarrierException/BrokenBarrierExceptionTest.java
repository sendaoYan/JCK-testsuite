/*
 * @test
 * @summary java.util.concurrent.BrokenBarrierException API conformance: constructors and inheritance.
 * @run main BrokenBarrierExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.concurrent.BrokenBarrierException;

public class BrokenBarrierExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBrokenBarrierException() {
        BrokenBarrierException e = new BrokenBarrierException("m");
        check(e instanceof Exception, "extends Exception");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new BrokenBarrierException().getMessage() == null, "no-arg ctor has null message");
    }

    // ---- white-box: constructor matrix / hierarchy / serializable shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<BrokenBarrierException> c = BrokenBarrierException.class;
        check(c.getSuperclass() == Exception.class, "getSuperclass() is exactly Exception");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception, not a RuntimeException");
        check(Serializable.class.isAssignableFrom(c), "Serializable via Throwable");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()), "class is not final (subclassable)");
        check(c.getConstructors().length == 2, "exactly 2 public constructors (locked ctor matrix)");
        check(c.getConstructor() != null, "() ctor present");
        check(c.getConstructor(String.class) != null, "(String) ctor present");
        boolean stringThrowable = false;
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            if (k.getParameterTypes().length == 2
                    && k.getParameterTypes()[0] == String.class
                    && k.getParameterTypes()[1] == Throwable.class) stringThrowable = true;
        }
        check(!stringThrowable, "no (String,Throwable) ctor in the public matrix");
    }

    // ---- message/cause/toString/fillInStackTrace contract ----
    static void testThrowableContract() {
        BrokenBarrierException e = new BrokenBarrierException("boom");
        check("boom".equals(e.getMessage()), "getMessage exact retention");
        check("java.util.concurrent.BrokenBarrierException: boom".equals(e.toString()),
              "toString is ClassName: message");
        check(e.getCause() == null, "no cause unless explicitly given");
        check(e.getSuppressed().length == 0, "no suppressed exceptions initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");
        e.setStackTrace(new java.lang.StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) honoured");
        java.lang.StackTraceElement ste =
            new java.lang.StackTraceElement("Fake", "m", "Fake.java", 42);
        e.setStackTrace(new java.lang.StackTraceElement[] { ste });
        check(e.getStackTrace()[0] == ste, "setStackTrace element retained");

        BrokenBarrierException e0 = new BrokenBarrierException();
        check(e0.getMessage() == null, "no-arg ctor message is null");
        check("java.util.concurrent.BrokenBarrierException".equals(e0.toString()),
              "toString with null message is just the class name");
        IllegalStateException cause = new IllegalStateException("root");
        check(e0.initCause(cause) == e0, "initCause returns this");
        check(e0.getCause() == cause, "initCause sets the cause");
        check("java.util.concurrent.BrokenBarrierException".equals(e0.toString()),
              "toString unaffected by initCause (message still null)");
        boolean secondInit = false;
        try { e0.initCause(new Exception()); }
        catch (IllegalStateException expected) { secondInit = true; }
        check(secondInit, "second initCause throws IllegalStateException");
    }

    // ---- serialization: serialVersionUID lock + round-trip ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(BrokenBarrierException.class).getSerialVersionUID()
                  == 7117394618823254244L, "serialVersionUID locked (7117394618823254244)");

        BrokenBarrierException orig = new BrokenBarrierException("boom");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
        Object copyObj = ois.readObject();
        ois.close();
        check(copyObj instanceof BrokenBarrierException, "round-trip type preserved");
        BrokenBarrierException copy = (BrokenBarrierException) copyObj;
        check("boom".equals(copy.getMessage()), "round-trip message preserved");
        check(copy.getCause() == null, "round-trip null cause stays null");
        check(orig.toString().equals(copy.toString()), "round-trip toString equal");

        BrokenBarrierException withCause = new BrokenBarrierException("wrapped");
        withCause.initCause(new IllegalStateException("root"));
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(withCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(
            new ByteArrayInputStream(bos2.toByteArray()));
        BrokenBarrierException copy2 = (BrokenBarrierException) ois2.readObject();
        ois2.close();
        check("wrapped".equals(copy2.getMessage()), "round-trip message with cause");
        check(copy2.getCause() instanceof IllegalStateException, "round-trip cause type");
        check("root".equals(copy2.getCause().getMessage()), "round-trip cause message");
    }

    public static void main(String[] args) throws Exception {
        testBrokenBarrierException();
        testWhiteBoxStructure();
        testThrowableContract();
        testSerializationContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
