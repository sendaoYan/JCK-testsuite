/*
 * @test
 * @summary javax.management.InstanceAlreadyExistsException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main InstanceAlreadyExistsExceptionTest
 */

import javax.management.InstanceAlreadyExistsException;
import javax.management.*;
import java.io.*;

public class InstanceAlreadyExistsExceptionTest {
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
        InstanceAlreadyExistsException noArg = new InstanceAlreadyExistsException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        InstanceAlreadyExistsException msg = new InstanceAlreadyExistsException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        InstanceAlreadyExistsException nullMsg = new InstanceAlreadyExistsException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(InstanceAlreadyExistsException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(InstanceAlreadyExistsException.class).getSerialVersionUID() == 8893743928912733931L,
                "serialVersionUID constant (" + 8893743928912733931L + ")");
        InstanceAlreadyExistsException backNo = deepRoundTrip(new InstanceAlreadyExistsException());
        check(backNo instanceof InstanceAlreadyExistsException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        InstanceAlreadyExistsException backMsg = deepRoundTrip(new InstanceAlreadyExistsException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("InstanceAlreadyExistsException");
    }

    public static interface ProbeMBean { int add(int x); }
    public static class Probe implements ProbeMBean {
        int c;
        public int add(int x) { c += x; return c; }
    }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.instancealready:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.registerMBean(new Probe(), name);
            check(false, "duplicate registerMBean must throw");
        } catch (InstanceAlreadyExistsException e) {
            passed++;
        }
        check(server.isRegistered(name), "state invariant: original registration survives");
        check(Integer.valueOf(5).equals(server.invoke(name, "add",
                new Object[] { Integer.valueOf(5) }, new String[] { "int" })),
                "state invariant: original MBean still functional");
    }

    public static void main(String[] args) throws Exception {
        InstanceAlreadyExistsException x = new InstanceAlreadyExistsException("dup");
        check("dup".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        InstanceAlreadyExistsException noArg = new InstanceAlreadyExistsException();
        check(noArg.getMessage() == null, "no-arg message null");
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
