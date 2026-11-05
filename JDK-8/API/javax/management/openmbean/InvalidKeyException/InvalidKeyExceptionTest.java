/*
 * @test
 * @summary javax.management.openmbean.InvalidKeyException conformance: hierarchy, ctor chain, serialization, real trigger point.
 * @run main InvalidKeyExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class InvalidKeyExceptionTest {
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
        javax.management.openmbean.InvalidKeyException x = new javax.management.openmbean.InvalidKeyException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.lang.Exception.class.isAssignableFrom(c), "is an Exception");
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(c), "extends java.lang.IllegalArgumentException");
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
        InvalidKeyException noArg = new InvalidKeyException();
        check(noArg.getMessage() == null, "no-arg message null");
        check(noArg.getCause() == null, "no-arg cause null");
        InvalidKeyException withMsg = new InvalidKeyException("boom");
        check("boom".equals(withMsg.getMessage()), "String ctor message");
        check(withMsg.getCause() == null, "String ctor cause null");
        // initCause on a legacy exception without cause
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.InvalidKeyException"), "toString prefix");
        check(withMsg.toString().contains("boom"), "toString contains message");
        // exception chaining preserved through constructor form has no cause variant (javap: only () and (String))
        check(InvalidKeyException.class.getConstructors().length == 2, "exactly two public constructors");
        check(InvalidKeyException.class.getConstructor() != null && InvalidKeyException.class.getConstructor(String.class) != null,
            "() and (String) constructors");
    }

    // ---- deepened: exact hierarchy (verified via javap on both JDKs) ----
    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.InvalidKeyException");
        check("java.lang.IllegalArgumentException".equals(c.getSuperclass().getName()), "direct superclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.lang.RuntimeException.class.isAssignableFrom(c), "checked vs unchecked");
    }

    // ---- deepened: serialization round-trip keeps class and message ----
    static void testSerialization() throws Exception {
        InvalidKeyException e = new InvalidKeyException("round-trip");
        InvalidKeyException e2 = (InvalidKeyException) roundTrip(e);
        check(e2.getClass() == InvalidKeyException.class, "round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "round-trip message preserved");
        check(ObjectStreamClass.lookup(InvalidKeyException.class).getSerialVersionUID() == 4224269443946322062L, "serialVersionUID locked");
        InvalidKeyException n2 = (InvalidKeyException) roundTrip(new InvalidKeyException());
        check(n2.getMessage() == null, "round-trip null message preserved");
    }

    // ---- deepened: real library trigger point ----
    static void testTrigger() throws Exception {
        // trigger: CompositeData.get with a key that is not an item name
        CompositeType ct = new CompositeType("T", "d", new String[] {"a"}, new String[] {"ad"},
            new OpenType[] {SimpleType.INTEGER});
        CompositeDataSupport cd = new CompositeDataSupport(ct, new String[] {"a"}, new Object[] {1});
        try {
            cd.get("missing");
            throw new AssertionError("expected InvalidKeyException from get(missing)");
        } catch (javax.management.openmbean.InvalidKeyException e) {
            check("Argument key=\"missing\" is not an existing item name for this CompositeData instance.".equals(e.getMessage()),
                "get(missing) trigger message");
        }
        // documented contrast: get(null) throws IllegalArgumentException, not InvalidKeyException
        try {
            cd.get(null);
            throw new AssertionError("expected IllegalArgumentException from get(null)");
        } catch (IllegalArgumentException e) {
            check(!(e instanceof javax.management.openmbean.InvalidKeyException), "get(null) is plain IAE");
            check("Argument key cannot be a null or empty String.".equals(e.getMessage()), "get(null) message");
        }
        // not a subclass of KeyAlreadyExistsException (both are siblings under IAE)
        check(!javax.management.openmbean.KeyAlreadyExistsException.class.isAssignableFrom(
            javax.management.openmbean.InvalidKeyException.class), "InvalidKeyException is not a KeyAlreadyExistsException");
    }
}
