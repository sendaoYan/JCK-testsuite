/*
 * @test
 * @summary javax.management.openmbean.KeyAlreadyExistsException conformance: hierarchy, ctor chain, serialization, real trigger point.
 * @run main KeyAlreadyExistsExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class KeyAlreadyExistsExceptionTest {
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
        javax.management.openmbean.KeyAlreadyExistsException x = new javax.management.openmbean.KeyAlreadyExistsException("msg");
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
        KeyAlreadyExistsException noArg = new KeyAlreadyExistsException();
        check(noArg.getMessage() == null, "no-arg message null");
        check(noArg.getCause() == null, "no-arg cause null");
        KeyAlreadyExistsException withMsg = new KeyAlreadyExistsException("boom");
        check("boom".equals(withMsg.getMessage()), "String ctor message");
        check(withMsg.getCause() == null, "String ctor cause null");
        // initCause on a legacy exception without cause
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.KeyAlreadyExistsException"), "toString prefix");
        check(withMsg.toString().contains("boom"), "toString contains message");
        // exception chaining preserved through constructor form has no cause variant (javap: only () and (String))
        check(KeyAlreadyExistsException.class.getConstructors().length == 2, "exactly two public constructors");
        check(KeyAlreadyExistsException.class.getConstructor() != null && KeyAlreadyExistsException.class.getConstructor(String.class) != null,
            "() and (String) constructors");
    }

    // ---- deepened: exact hierarchy (verified via javap on both JDKs) ----
    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.KeyAlreadyExistsException");
        check("java.lang.IllegalArgumentException".equals(c.getSuperclass().getName()), "direct superclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.lang.RuntimeException.class.isAssignableFrom(c), "checked vs unchecked");
    }

    // ---- deepened: serialization round-trip keeps class and message ----
    static void testSerialization() throws Exception {
        KeyAlreadyExistsException e = new KeyAlreadyExistsException("round-trip");
        KeyAlreadyExistsException e2 = (KeyAlreadyExistsException) roundTrip(e);
        check(e2.getClass() == KeyAlreadyExistsException.class, "round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "round-trip message preserved");
        check(ObjectStreamClass.lookup(KeyAlreadyExistsException.class).getSerialVersionUID() == 1845183636745282866L, "serialVersionUID locked");
        KeyAlreadyExistsException n2 = (KeyAlreadyExistsException) roundTrip(new KeyAlreadyExistsException());
        check(n2.getMessage() == null, "round-trip null message preserved");
    }

    // ---- deepened: real library trigger point ----
    static void testTrigger() throws Exception {
        // trigger: putAll(CompositeData[]) with two rows having the same calculated index
        CompositeType row = new CompositeType("Row", "d", new String[] {"k", "v"},
            new String[] {"kd", "vd"}, new OpenType[] {SimpleType.STRING, SimpleType.STRING});
        TabularType tt = new TabularType("T", "d", row, new String[] {"k"});
        TabularDataSupport td = new TabularDataSupport(tt);
        try {
            td.putAll(new javax.management.openmbean.CompositeData[] {
                new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"same", "v1"}),
                new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"same", "v2"})});
            throw new AssertionError("expected KeyAlreadyExistsException from duplicate index in putAll");
        } catch (javax.management.openmbean.KeyAlreadyExistsException e) {
            check("Argument elements values[1] and values[0] have the same indexes, calculated according to this TabularData instance's tabularType.".equals(e.getMessage()),
                "putAll duplicate-index trigger message");
        }
        // trigger 2: single put(CompositeData) with an index already present also throws (no Map-style replace)
        td.put(new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"dup", "a1"}));
        try {
            td.put(new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"dup", "a2"}));
            throw new AssertionError("expected KeyAlreadyExistsException from put with existing index");
        } catch (javax.management.openmbean.KeyAlreadyExistsException e) {
            check("Argument value's index, calculated according to this TabularData instance's tabularType, already refers to a value in this table.".equals(e.getMessage()),
                "put existing-index trigger message");
        }
        // trigger 3: putAll batch colliding with a row already in the table gives the same message
        try {
            td.putAll(new javax.management.openmbean.CompositeData[] {
                new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"other", "x"}),
                new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"dup", "y"})});
            throw new AssertionError("expected KeyAlreadyExistsException from putAll against existing row");
        } catch (javax.management.openmbean.KeyAlreadyExistsException e) {
            check("Argument value's index, calculated according to this TabularData instance's tabularType, already refers to a value in this table.".equals(e.getMessage()),
                "putAll against existing row trigger message");
        }
        // trigger 4: the java.util.Map view put(Object[],CompositeData) throws the same exception (no replace either)
        try {
            td.put(new Object[] {"dup"}, new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"dup", "a3"}));
            throw new AssertionError("expected KeyAlreadyExistsException from Map-view put");
        } catch (javax.management.openmbean.KeyAlreadyExistsException e) {
            check("Argument value's index, calculated according to this TabularData instance's tabularType, already refers to a value in this table.".equals(e.getMessage()),
                "Map-view put trigger message");
        }
        check("a1".equals(td.get(new Object[] {"dup"}).get("v")), "original value untouched by failed puts");
        // failed putAll inserted nothing: batch validation happens before any mutation (atomic)
        check(td.size() == 1, "failed putAll left the table unchanged (validated before any mutation)");
        check(td.get(new Object[] {"other"}) == null, "non-colliding row of the failed batch was not inserted");
        // re-put succeeds after remove: removal is the only path back to a free index
        td.remove(new Object[] {"dup"});
        td.put(new CompositeDataSupport(row, new String[] {"k", "v"}, new Object[] {"dup", "fresh"}));
        check("fresh".equals(td.get(new Object[] {"dup"}).get("v")), "put succeeds after remove");
    }
}
