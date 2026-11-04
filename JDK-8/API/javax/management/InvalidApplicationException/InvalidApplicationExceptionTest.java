/*
 * @test
 * @summary javax.management.InvalidApplicationException conformance: exception hierarchy and construction.
 * @run main InvalidApplicationExceptionTest
 */
import java.io.*;

import javax.management.InvalidApplicationException;
import javax.management.ObjectName;

public class InvalidApplicationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deepened (2026-11): ctor / serial field / hierarchy / serialization ----

    static void testDeepContract() throws Exception {
        InvalidApplicationException s = new InvalidApplicationException("jck-val");
        check(s.getMessage() == null, "no detail message (ctor stores val, not a message)");
        check(s instanceof Exception, "extends java.lang.Exception");
        check(!javax.management.JMException.class.isInstance(s), "NOT a JMException (outside the JM hierarchy)");
        check(s instanceof java.io.Serializable, "Serializable");
        // javap-verified both sides: no public accessor for val; it is a private serial field
        java.io.ObjectStreamField[] fields =
                java.io.ObjectStreamClass.lookup(InvalidApplicationException.class).getFields();
        check(fields.length == 1 && "val".equals(fields[0].getName())
                && Object.class.equals(fields[0].getType()), "single serial field 'val' of type Object");
        check(java.io.ObjectStreamClass.lookup(InvalidApplicationException.class).getSerialVersionUID()
                == -3048022274675537269L, "serialVersionUID constant");
    }


    @SuppressWarnings("unchecked")
    static <T extends Throwable> T deepRoundTrip(T t) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    static void testDeepRoundTripValues() throws Exception {
        Object[] vals = { "str-val", Integer.valueOf(3), new ObjectName("jck.iae:k=v") };
        for (Object v : vals) {
            InvalidApplicationException back = deepRoundTrip(new InvalidApplicationException(v));
            check(back instanceof InvalidApplicationException, "round-trip class for " + v.getClass().getSimpleName());
            check(back.getMessage() == null, "round-trip message stays null");
        }
    }

    public static void main(String[] args) throws Exception {
        javax.management.InvalidApplicationException x = new javax.management.InvalidApplicationException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.Exception").isAssignableFrom(c), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepContract();
        testDeepRoundTripValues();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
