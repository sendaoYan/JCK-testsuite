/*
 * @test
 * @summary javax.management.modelmbean.InvalidTargetObjectTypeException conformance: white-box structure + black-box behavior.
 * @run main InvalidTargetObjectTypeExceptionTest
 */

import javax.management.modelmbean.InvalidTargetObjectTypeException;

public class InvalidTargetObjectTypeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.InvalidTargetObjectTypeException");
        check(Exception.class.isAssignableFrom(c), "extends Exception");
        check(c.getSuperclass() == Exception.class, "direct superclass is Exception");
        check(c.getConstructors().length >= 3, "has all three constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InvalidTargetObjectTypeException e1 = new InvalidTargetObjectTypeException("boom");
        check("InvalidTargetObjectTypeException: boom".equals(e1.getMessage()), "String message");
        check(e1 instanceof Exception, "instance of Exception");

        InvalidTargetObjectTypeException e2 = new InvalidTargetObjectTypeException();
        check("InvalidTargetObjectTypeException: ".equals(e2.getMessage()), "no-arg message");

        Exception cause = new Exception("cause");
        InvalidTargetObjectTypeException e3 = new InvalidTargetObjectTypeException(cause, "msg2");
        check(e3.getMessage().startsWith("InvalidTargetObjectTypeException: msg2"), "target message prefix");
        check(e3.getMessage().contains("triggered by:"), "target message embeds cause");

        InvalidTargetObjectTypeException e4 = new InvalidTargetObjectTypeException(null, "msg3");
        check("InvalidTargetObjectTypeException: msg3".equals(e4.getMessage()), "null cause message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepRealTrigger();
        testDeepSerializationAndHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): real trigger, serialization, checked-exception contract ----

    static void testDeepRealTrigger() throws Exception {
        // the real source: RequiredModelMBean.setManagedResource with an unsupported target type
        javax.management.modelmbean.RequiredModelMBean rmm = new javax.management.modelmbean.RequiredModelMBean();
        try {
            rmm.setManagedResource(new Object(), "Bogus");
            check(false, "bogus target type must throw");
        } catch (InvalidTargetObjectTypeException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bogus"),
                    "message contains the offending type: " + e.getMessage());
        }
        try {
            rmm.setManagedResource(new Object(), null);
            check(false, "null target type must throw");
        } catch (InvalidTargetObjectTypeException e) {
            check(true, "null target type -> InvalidTargetObjectTypeException");
        }
        // declared as a checked exception of ModelMBean.setManagedResource
        java.lang.reflect.Method m = javax.management.modelmbean.ModelMBean.class
                .getMethod("setManagedResource", Object.class, String.class);
        boolean declared = false;
        for (Class<?> ex : m.getExceptionTypes())
            if (ex == InvalidTargetObjectTypeException.class) declared = true;
        check(declared, "InvalidTargetObjectTypeException is declared on ModelMBean.setManagedResource");
    }

    static void testDeepSerializationAndHierarchy() throws Exception {
        check(!java.lang.RuntimeException.class.isAssignableFrom(InvalidTargetObjectTypeException.class),
                "not a RuntimeException (checked)");
        check(InvalidTargetObjectTypeException.class.getConstructors().length == 3,
                "exactly three public constructors");
        InvalidTargetObjectTypeException e = new InvalidTargetObjectTypeException(new Exception("in"), "m");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        InvalidTargetObjectTypeException back = (InvalidTargetObjectTypeException)
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(e.getMessage().equals(back.getMessage()), "serialization round-trip preserves the message");
        check(back.toString().contains("InvalidTargetObjectTypeException"), "toString contains class name");
    }
}
