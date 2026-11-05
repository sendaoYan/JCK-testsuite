/*
 * @test
 * @summary javax.management.modelmbean.InvalidTargetObjectTypeException conformance: exception hierarchy and construction.
 * @run main InvalidTargetObjectTypeExceptionTest
 */

import javax.management.modelmbean.InvalidTargetObjectTypeException;

public class InvalidTargetObjectTypeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.modelmbean.InvalidTargetObjectTypeException x = new javax.management.modelmbean.InvalidTargetObjectTypeException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.Exception").isAssignableFrom(c), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
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
