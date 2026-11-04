/*
 * @test
 * @summary javax.management.MalformedObjectNameException conformance: full
 *          contract coverage. message construction, OperationsException/JMException
 *          hierarchy, no-arg ctor, and ObjectName.parse integration point.
 * @run main MalformedObjectNameExceptionTest
 */

import javax.management.MalformedObjectNameException;
import javax.management.*;
import java.io.*;

public class MalformedObjectNameExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }


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
        MalformedObjectNameException x = new MalformedObjectNameException("msg");
        check("msg".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");
        check(x.getCause() == null, "no cause by default");

        MalformedObjectNameException noArg = new MalformedObjectNameException();
        check(noArg.getMessage() == null, "no-arg message null");
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
