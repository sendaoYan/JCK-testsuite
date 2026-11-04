/*
 * @test
 * @summary javax.management.MalformedObjectNameException conformance: white-box structure + black-box behavior.
 * @run main MalformedObjectNameExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class MalformedObjectNameExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MalformedObjectNameException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.OperationsException".equals(c.getSuperclass().getName()), "superclass javax.management.OperationsException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");

        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor();
        check(ctor != null, "no-arg constructor");
        ctor = c.getConstructor(String.class);
        check(ctor != null, "String constructor");
    }
    static void testBlackBox() throws Exception {
        Exception e = new MalformedObjectNameException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("MalformedObjectNameException"), "toString contains class name");
        check(e instanceof javax.management.OperationsException, "instanceof OperationsException");
        check(e instanceof javax.management.JMException, "instanceof JMException");
        Exception e2 = new MalformedObjectNameException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("MalformedObjectNameException"), "no-arg toString class name");
        Exception e3 = new MalformedObjectNameException((String) null);
        check(e3.getMessage() == null, "null String message null");

        // Real production path: ObjectName parsing.
        try {
            new javax.management.ObjectName("bad:::name");
            check(false, "ObjectName should throw MalformedObjectNameException");
        } catch (javax.management.MalformedObjectNameException ex) {
            check("Invalid character ':' in key part of property".equals(ex.getMessage()),
                  "MalformedObjectNameException precise message");
        }

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

    // ---- deepened (2026-11): ctor chain / hierarchy / serialVersionUID / serialization ----

    static void testDeepCtorChain() {
        MalformedObjectNameException noArg = new MalformedObjectNameException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        MalformedObjectNameException msg = new MalformedObjectNameException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        MalformedObjectNameException nullMsg = new MalformedObjectNameException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(MalformedObjectNameException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(MalformedObjectNameException.class).getSerialVersionUID() == -572689714442915824L,
                "serialVersionUID constant (" + -572689714442915824L + ")");
        MalformedObjectNameException backNo = deepRoundTrip(new MalformedObjectNameException());
        check(backNo instanceof MalformedObjectNameException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        MalformedObjectNameException backMsg = deepRoundTrip(new MalformedObjectNameException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("MalformedObjectNameException");
    }

    // ---- deepened (2026-11): real trigger points in ObjectName/ObjectInstance parsing ----

    static void testDeepLiveTrigger() throws Exception {
        try {
            new ObjectName("a=b:c");
            check(false, "'=' inside the domain must be malformed");
        } catch (MalformedObjectNameException e) {
            check("Unterminated key property part".equals(e.getMessage()), "trigger: '=' in domain message");
        }
        try {
            new ObjectName("d:k=\"bad\\escape\"");
            check(false, "invalid escape inside a quoted value must be malformed");
        } catch (MalformedObjectNameException e) {
            check("Invalid escape sequence '\\e' in quoted value".equals(e.getMessage()), "trigger: bad escape message");
        }
        try {
            new ObjectInstance("no-colon-here", "c");
            check(false, "ObjectInstance(String,...) with a malformed name must throw");
        } catch (MalformedObjectNameException e) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
