/*
 * @test
 * @summary javax.management.InvalidApplicationException conformance: white-box structure + black-box behavior.
 * @run main InvalidApplicationExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class InvalidApplicationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.InvalidApplicationException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(c.getConstructor(Object.class) != null, "Object constructor");
    }

    static void testBlackBox() throws Exception {
        Object val = new Object();
        InvalidApplicationException e = new InvalidApplicationException(val);
        check(e.getMessage() == null, "no detail message");
        check("javax.management.InvalidApplicationException".equals(e.toString()), "toString is bare class name");
        check(e instanceof Exception, "instanceof Exception");

        InvalidApplicationException e2 = new InvalidApplicationException(null);
        check(e2.getMessage() == null, "null value still no message");
    }


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
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        testDeepRoundTripValues();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
