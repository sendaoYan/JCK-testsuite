/*
 * @test
 * @summary java.lang.instrument.UnmodifiableClassException conformance: hierarchy and ctors.
 * @run main UnmodifiableClassExceptionTest
 */

import java.lang.instrument.UnmodifiableClassException;

public class UnmodifiableClassExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnmodifiableClassException() {
        check(Exception.class.isAssignableFrom(UnmodifiableClassException.class),
              "extends Exception");
        UnmodifiableClassException e = new UnmodifiableClassException();
        check(e.getMessage() == null, "default message");
        UnmodifiableClassException e2 = new UnmodifiableClassException("unmodifiable");
        check("unmodifiable".equals(e2.getMessage()), "custom message");
    }


    static void testDeepConstructorSurface() throws Exception {
        check(UnmodifiableClassException.class.getConstructors().length == 2,
              "exactly two public constructors: () and (String)");
        check(Exception.class.isAssignableFrom(UnmodifiableClassException.class), "extends Exception");
        UnmodifiableClassException e = new UnmodifiableClassException("locked");
        check("locked".equals(e.getMessage()), "message round-trip");
        check(e.getCause() == null, "no cause");
        check(e instanceof java.io.Serializable, "serializable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        UnmodifiableClassException back = (UnmodifiableClassException) ois.readObject();
        ois.close();
        check("locked".equals(back.getMessage()), "serialization preserves the message");
        check(new UnmodifiableClassException().getMessage() == null, "no-arg message null");
    }

    public static void main(String[] args) throws Exception {
        testUnmodifiableClassException();
        testDeepConstructorSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
