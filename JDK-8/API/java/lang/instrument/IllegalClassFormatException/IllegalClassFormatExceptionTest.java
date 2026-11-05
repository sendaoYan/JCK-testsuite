/*
 * @test
 * @summary java.lang.instrument.IllegalClassFormatException conformance: hierarchy and ctors.
 * @run main IllegalClassFormatExceptionTest
 */

import java.lang.instrument.IllegalClassFormatException;

public class IllegalClassFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalClassFormatException() {
        check(Exception.class.isAssignableFrom(IllegalClassFormatException.class),
              "extends Exception");
        IllegalClassFormatException e = new IllegalClassFormatException();
        check(e.getMessage() == null, "default message");
        IllegalClassFormatException e2 = new IllegalClassFormatException("bad format");
        check("bad format".equals(e2.getMessage()), "custom message");
    }


    static void testDeepConstructorSurface() throws Exception {
        // Only () and (String) exist on both JDK 8 and JDK 27 (no cause constructors)
        check(IllegalClassFormatException.class.getConstructors().length == 2,
              "exactly two public constructors: () and (String)");
        check(Exception.class.isAssignableFrom(IllegalClassFormatException.class), "extends Exception");
        IllegalClassFormatException e = new IllegalClassFormatException("bad bytes");
        check("bad bytes".equals(e.getMessage()), "message round-trip");
        check(e.getCause() == null, "no cause");
        check(e instanceof java.io.Serializable, "serializable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        IllegalClassFormatException back = (IllegalClassFormatException) ois.readObject();
        ois.close();
        check("bad bytes".equals(back.getMessage()), "serialization preserves the message");
        check(java.io.ObjectStreamClass.lookup(IllegalClassFormatException.class)
                .getSerialVersionUID() != 0L, "serialVersionUID present");
        check(new IllegalClassFormatException().getMessage() == null, "no-arg message null");
        // fillInStackTrace / initCause still work (Throwable contract)
        RuntimeException cause = new RuntimeException("root");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause late binding");
    }

    public static void main(String[] args) throws Exception {
        testIllegalClassFormatException();
        testDeepConstructorSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
