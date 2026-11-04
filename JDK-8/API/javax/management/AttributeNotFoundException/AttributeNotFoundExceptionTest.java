/*
 * @test
 * @summary javax.management.AttributeNotFoundException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main AttributeNotFoundExceptionTest
 */

import javax.management.AttributeNotFoundException;
import java.io.*;
import javax.management.*;

public class AttributeNotFoundExceptionTest {
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
        AttributeNotFoundException noArg = new AttributeNotFoundException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        AttributeNotFoundException msg = new AttributeNotFoundException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        AttributeNotFoundException nullMsg = new AttributeNotFoundException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(AttributeNotFoundException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(AttributeNotFoundException.class).getSerialVersionUID() == 6511584241791106926L,
                "serialVersionUID constant (" + 6511584241791106926L + ")");
        AttributeNotFoundException backNo = deepRoundTrip(new AttributeNotFoundException());
        check(backNo instanceof AttributeNotFoundException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        AttributeNotFoundException backMsg = deepRoundTrip(new AttributeNotFoundException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("AttributeNotFoundException");
    }

    public static interface ProbeMBean { int getCount(); }
    public static class Probe implements ProbeMBean { public int getCount() { return 42; } }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.attrnotfound:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.getAttribute(name, "Nope");
            check(false, "getAttribute on unknown attribute must throw");
        } catch (AttributeNotFoundException e) {
            check("No such attribute: Nope".equals(e.getMessage()), "live trigger getAttribute message");
        }
        try {
            server.setAttribute(name, new Attribute("Nope", Integer.valueOf(1)));
            check(false, "setAttribute on unknown attribute must throw");
        } catch (AttributeNotFoundException e) {
            check("No such attribute: Nope".equals(e.getMessage()), "live trigger setAttribute message");
        }
        check(Integer.valueOf(42).equals(server.getAttribute(name, "Count")), "state invariant: reads still work");
    }

    public static void main(String[] args) throws Exception {
        AttributeNotFoundException x = new AttributeNotFoundException("attr");
        check("attr".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        AttributeNotFoundException noArg = new AttributeNotFoundException();
        check(noArg.getMessage() == null, "no-arg message null");
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
