/*
 * @test
 * @summary javax.management.openmbean.InvalidOpenTypeException conformance: hierarchy, ctor chain, serialization, real trigger point.
 * @run main InvalidOpenTypeExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidOpenTypeException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class InvalidOpenTypeExceptionTest {
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
        javax.management.openmbean.InvalidOpenTypeException x = new javax.management.openmbean.InvalidOpenTypeException("msg");
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
        InvalidOpenTypeException noArg = new InvalidOpenTypeException();
        check(noArg.getMessage() == null, "no-arg message null");
        check(noArg.getCause() == null, "no-arg cause null");
        InvalidOpenTypeException withMsg = new InvalidOpenTypeException("boom");
        check("boom".equals(withMsg.getMessage()), "String ctor message");
        check(withMsg.getCause() == null, "String ctor cause null");
        // initCause on a legacy exception without cause
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.InvalidOpenTypeException"), "toString prefix");
        check(withMsg.toString().contains("boom"), "toString contains message");
        // exception chaining preserved through constructor form has no cause variant (javap: only () and (String))
        check(InvalidOpenTypeException.class.getConstructors().length == 2, "exactly two public constructors");
        check(InvalidOpenTypeException.class.getConstructor() != null && InvalidOpenTypeException.class.getConstructor(String.class) != null,
            "() and (String) constructors");
    }

    // ---- deepened: exact hierarchy (verified via javap on both JDKs) ----
    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.InvalidOpenTypeException");
        check("java.lang.IllegalArgumentException".equals(c.getSuperclass().getName()), "direct superclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.lang.RuntimeException.class.isAssignableFrom(c), "checked vs unchecked");
    }

    // ---- deepened: serialization round-trip keeps class and message ----
    static void testSerialization() throws Exception {
        InvalidOpenTypeException e = new InvalidOpenTypeException("round-trip");
        InvalidOpenTypeException e2 = (InvalidOpenTypeException) roundTrip(e);
        check(e2.getClass() == InvalidOpenTypeException.class, "round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "round-trip message preserved");
        check(ObjectStreamClass.lookup(InvalidOpenTypeException.class).getSerialVersionUID() == -2837312755412327534L, "serialVersionUID locked");
        InvalidOpenTypeException n2 = (InvalidOpenTypeException) roundTrip(new InvalidOpenTypeException());
        check(n2.getMessage() == null, "round-trip null message preserved");
    }

    // ---- deepened: real library trigger point ----
    static void testTrigger() throws Exception {
        // trigger: TabularDataSupport.put with a row whose CompositeType differs from the row type
        CompositeType rowA = new CompositeType("A", "d", new String[] {"k"}, new String[] {"kd"},
            new OpenType[] {SimpleType.STRING});
        CompositeType rowB = new CompositeType("B", "d", new String[] {"k"}, new String[] {"kd"},
            new OpenType[] {SimpleType.STRING});
        TabularType tt = new TabularType("T", "d", rowA, new String[] {"k"});
        TabularDataSupport td = new TabularDataSupport(tt);
        try {
            td.put(new CompositeDataSupport(rowB, new String[] {"k"}, new Object[] {"v"}));
            throw new AssertionError("expected InvalidOpenTypeException from mismatched row type");
        } catch (javax.management.openmbean.InvalidOpenTypeException e) {
            check(e.getMessage() != null && e.getMessage().contains("is not assignable to this TabularData instance's row type"),
                "TabularDataSupport row-type trigger message");
        }
        // trigger 2: OpenType.isValue used directly reports via this exception family semantics (documented unchecked signal)
        check(!SimpleType.INTEGER.isValue("not-an-integer"), "isValue false for wrong type");
    }
}
