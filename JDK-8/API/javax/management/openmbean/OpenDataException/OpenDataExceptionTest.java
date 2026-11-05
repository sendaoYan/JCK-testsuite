/*
 * @test
 * @summary javax.management.openmbean.OpenDataException conformance: hierarchy, ctor chain, serialization, real trigger point.
 * @run main OpenDataExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class OpenDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    public static void main(String[] args) throws Exception {
        javax.management.openmbean.OpenDataException x = new javax.management.openmbean.OpenDataException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.lang.Exception.class.isAssignableFrom(c), "is an Exception");
        check(Class.forName("javax.management.JMException").isAssignableFrom(c), "extends javax.management.JMException");
        x.toString(); x.getMessage(); x.getCause(); x.getStackTrace();
        // ---- deepened: batch g ----
        testConstructors();
        testHierarchy();
        testSerialization();
        testTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: constructor chain and cause semantics ----
    static void testConstructors() throws Exception {
        OpenDataException noArg = new OpenDataException();
        check(noArg.getMessage() == null, "no-arg message null");
        check(noArg.getCause() == null, "no-arg cause null");
        OpenDataException withMsg = new OpenDataException("boom");
        check("boom".equals(withMsg.getMessage()), "String ctor message");
        check(withMsg.getCause() == null, "String ctor cause null");
        // initCause on a legacy exception without cause
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.OpenDataException"), "toString prefix");
        check(withMsg.toString().contains("boom"), "toString contains message");
        // exception chaining preserved through constructor form has no cause variant (javap: only () and (String))
        check(OpenDataException.class.getConstructors().length == 2, "exactly two public constructors");
        check(OpenDataException.class.getConstructor() != null && OpenDataException.class.getConstructor(String.class) != null,
            "() and (String) constructors");
    }

    // ---- deepened: exact hierarchy (verified via javap on both JDKs) ----
    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenDataException");
        check("javax.management.JMException".equals(c.getSuperclass().getName()), "direct superclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(!java.lang.RuntimeException.class.isAssignableFrom(c), "checked vs unchecked");
    }

    // ---- deepened: serialization round-trip keeps class and message ----
    static void testSerialization() throws Exception {
        OpenDataException e = new OpenDataException("round-trip");
        OpenDataException e2 = (OpenDataException) roundTrip(e);
        check(e2.getClass() == OpenDataException.class, "round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "round-trip message preserved");
        check(ObjectStreamClass.lookup(OpenDataException.class).getSerialVersionUID() == 8346311255433349870L, "serialVersionUID locked");
        OpenDataException n2 = (OpenDataException) roundTrip(new OpenDataException());
        check(n2.getMessage() == null, "round-trip null message preserved");
    }

    // ---- deepened: real library trigger point ----
    static void testTrigger() throws Exception {
        // trigger 1: CompositeDataSupport with a value of the wrong open type
        CompositeType ct = new CompositeType("T", "d", new String[] {"a"}, new String[] {"ad"},
            new OpenType[] {SimpleType.INTEGER});
        try {
            new CompositeDataSupport(ct, new String[] {"a"}, new Object[] {"str"});
            throw new AssertionError("expected OpenDataException from wrong item value type");
        } catch (javax.management.openmbean.OpenDataException e) {
            check("Argument value of wrong type for item a: value str, type javax.management.openmbean.SimpleType(name=java.lang.Integer)".equals(e.getMessage()),
                "CompositeDataSupport wrong-value trigger message");
        }
        // trigger 2: CompositeDataSupport with item names not matching the type
        try {
            new CompositeDataSupport(ct, new String[] {"b"}, new Object[] {1});
            throw new AssertionError("expected OpenDataException from item name mismatch");
        } catch (javax.management.openmbean.OpenDataException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Item names do not match CompositeType"),
                "CompositeDataSupport item-name trigger message");
        }
        // trigger 3: OpenMBeanParameterInfoSupport default value of wrong type (constructor-time check)
        @SuppressWarnings("rawtypes") OpenType rawInt = SimpleType.INTEGER;
        try {
            new javax.management.openmbean.OpenMBeanParameterInfoSupport("p", "d", rawInt, "str");
            throw new AssertionError("expected OpenDataException from info default value type");
        } catch (javax.management.openmbean.OpenDataException e) {
            check(e.getMessage().contains("defaultValue"), "info default-value trigger");
        }
    }
}
