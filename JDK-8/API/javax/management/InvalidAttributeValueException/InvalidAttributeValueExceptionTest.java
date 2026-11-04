/*
 * @test
 * @summary javax.management.InvalidAttributeValueException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main InvalidAttributeValueExceptionTest
 */

import javax.management.InvalidAttributeValueException;
import javax.management.*;
import java.io.*;

public class InvalidAttributeValueExceptionTest {
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
        InvalidAttributeValueException noArg = new InvalidAttributeValueException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        InvalidAttributeValueException msg = new InvalidAttributeValueException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        InvalidAttributeValueException nullMsg = new InvalidAttributeValueException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(InvalidAttributeValueException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(InvalidAttributeValueException.class).getSerialVersionUID() == 2164571879317142449L,
                "serialVersionUID constant (" + 2164571879317142449L + ")");
        InvalidAttributeValueException backNo = deepRoundTrip(new InvalidAttributeValueException());
        check(backNo instanceof InvalidAttributeValueException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        InvalidAttributeValueException backMsg = deepRoundTrip(new InvalidAttributeValueException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("InvalidAttributeValueException");
    }

    public static interface ProbeMBean { int getCount(); void setCount(int c); }
    public static class Probe implements ProbeMBean {
        int count = 11;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
    }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.invalidattrvalue:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.setAttribute(name, new Attribute("Count", "not-an-int"));
            check(false, "wrong-type attribute value must throw");
        } catch (InvalidAttributeValueException e) {
            check("Invalid value for attribute Count: not-an-int".equals(e.getMessage()), "live trigger message");
        }
        check(Integer.valueOf(11).equals(server.getAttribute(name, "Count")), "state invariant: value unchanged after rejected write");
        server.setAttribute(name, new Attribute("Count", Integer.valueOf(3)));
        check(Integer.valueOf(3).equals(server.getAttribute(name, "Count")), "valid write still works after rejection");
    }

    public static void main(String[] args) throws Exception {
        InvalidAttributeValueException x = new InvalidAttributeValueException("bad");
        check("bad".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        InvalidAttributeValueException noArg = new InvalidAttributeValueException();
        check(noArg.getMessage() == null, "no-arg message null");
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
